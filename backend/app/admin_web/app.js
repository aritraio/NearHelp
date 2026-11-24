'use strict';
const $ = id => document.getElementById(id);
const API = '/api/v1';
let accessToken = '', refreshToken = '', currentAdmin = null;
let offset = 0, queueOffset = 0, total = 0, queueTotal = 0, users = [], tab = 'users';
let timer, searchTimer, toastTimer, refreshPromise, pendingAction, syncing = false, userRequest = 0, queueRequest = 0;
const pageSize = 20;
const fmt = new Intl.NumberFormat();
const date = value => value ? new Date(value).toLocaleDateString(undefined, {day:'numeric', month:'short', year:'numeric'}) : '—';
function node(tag, text, className) {
  const element = document.createElement(tag);
  if (text !== undefined) element.textContent = text;
  if (className) element.className = className;
  return element;
}
function showNotice(message) {
  $('notice').textContent = message; $('notice').hidden = false;
  clearTimeout(toastTimer); toastTimer = setTimeout(() => { $('notice').hidden = true; }, 5000);
}
function errorMessage(data) {
  if (Array.isArray(data.detail)) return data.detail.map(item => `${item.loc.slice(1).join('.')}: ${item.msg}`).join('; ');
  return typeof data.detail === 'string' ? data.detail : 'The request could not be completed.';
}
async function api(path, options = {}, retry = true) {
  const response = await fetch(`${API}${path}`, {
    ...options, cache: 'no-store', headers: {'Content-Type':'application/json', ...(accessToken ? {Authorization:`Bearer ${accessToken}`} : {}), ...options.headers}
  });
  if (response.status === 401 && refreshToken && retry) {
    if (!refreshPromise) refreshPromise = (async () => {
      const res = await fetch(`${API}/auth/refresh`, {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({refresh_token:refreshToken})});
      if (!res.ok) throw new Error('Your session expired. Please sign in again.');
      const data = await res.json(); accessToken = data.access_token; refreshToken = data.refresh_token;
    })().finally(() => { refreshPromise = null; });
    try { await refreshPromise; } catch (error) { signOut(); throw error; }
    return api(path, options, false);
  }
  if (!response.ok) {
    const data = await response.json().catch(() => ({}));
    if ([401,403].includes(response.status) && currentAdmin) signOut();
    throw new Error(errorMessage(data));
  }
  return response.status === 204 ? null : response.json();
}
function signOut() {
  clearInterval(timer); accessToken = ''; refreshToken = ''; currentAdmin = null;
  userRequest++; queueRequest++; users = []; $('user-rows').replaceChildren(); $('queue-list').replaceChildren();
  for (const dialog of document.querySelectorAll('dialog')) dialog.close();
  $('dashboard').hidden = true; $('login-view').hidden = false; $('login-form').reset();
}
$('logout').addEventListener('click', signOut);
$('login-form').addEventListener('submit', async event => {
  event.preventDefault(); const button = event.submitter; button.disabled = true; $('login-error').textContent = '';
  try {
    const data = await api('/auth/login', {method:'POST', body:JSON.stringify({email:$('login-email').value.trim(), password:$('login-password').value})});
    accessToken = data.access_token; refreshToken = data.refresh_token;
    currentAdmin = await api('/admin/me');
    $('admin-name').textContent = currentAdmin.name || 'Administrator';
    $('login-form').reset(); $('login-view').hidden = true; $('dashboard').hidden = false;
    offset = 0; queueOffset = 0;
    await sync(); timer = setInterval(sync, 10000);
  } catch (error) { signOut(); $('login-error').textContent = error.message; }
  finally { button.disabled = false; }
});
async function loadStats() {
  const stats = await api('/admin/stats');
  if (!currentAdmin) return;
  const mapping = {'total-users':'total_users', 'online-users':'online_users', 'active-users':'active_users', 'pending-reviews':'pending_verifications', 'active-emergencies':'active_emergencies', 'new-users':'new_users_today', 'admin-users':'admin_users', 'queue-badge':'pending_verifications'};
  for (const [id, key] of Object.entries(mapping)) $(id).textContent = fmt.format(stats[key]);
}
function tag(text, className) { return node('span', text, `tag ${className || ''}`); }
function action(text, type, id, disabled = false) {
  const button = node('button', text, `row-action ${type === 'delete' ? 'delete' : ''}`);
  button.type = 'button'; button.dataset.action = type; button.dataset.id = id; button.disabled = disabled;
  return button;
}
async function loadUsers() {
  const request = ++userRequest;
  const params = new URLSearchParams({q:$('search').value, state:$('state-filter').value, role:$('role-filter').value, user_type:$('type-filter').value, limit:pageSize, offset});
  const data = await api(`/admin/users?${params}`);
  if (request !== userRequest || !currentAdmin) return;
  if (offset && offset >= data.total) {offset = Math.max(0, Math.floor((data.total-1)/pageSize)*pageSize); return loadUsers();}
  users = data.users; total = data.total; $('user-total').textContent = fmt.format(total);
  const rows = users.map(user => {
    const row = node('tr');
    const cell = node('td'); const member = node('div', undefined, 'member');
    member.append(node('div', (user.name || user.email || '?').slice(0,1).toUpperCase(), 'avatar'));
    const identity = node('div'); identity.append(node('strong', user.name || 'Anonymous member'), node('small', user.email || user.phone || 'Anonymous emergency account'));
    member.append(identity); cell.append(member); row.append(cell);
    const state = node('td'); state.append(tag(!user.is_active ? 'Suspended' : user.is_online ? '● Online' : 'Enabled', !user.is_active ? 'suspended' : user.is_online ? 'online' : '')); row.append(state);
    const role = node('td'); role.append(tag(user.is_superuser ? 'Administrator' : 'Member', user.is_superuser ? 'admin' : '')); row.append(role);
    const trust = node('td'); const score = node('div', undefined, 'trust');
    const meter = node('progress'); meter.max = 100; meter.value = user.trust_score; meter.setAttribute('aria-label', 'Trust score');
    score.append(meter, node('span', String(user.trust_score))); trust.append(score); row.append(trust, node('td', date(user.created_at)));
    const controls = node('td'); const group = node('div', undefined, 'row-actions');
    group.append(action('Edit', 'edit', user.id), action(user.is_active ? 'Suspend' : 'Enable', 'toggle', user.id, user.id === currentAdmin.id), action('Delete', 'delete', user.id, user.id === currentAdmin.id));
    controls.append(group); row.append(controls); return row;
  });
  $('user-rows').replaceChildren(...rows); $('users-empty').hidden = total !== 0;
  $('page-label').textContent = total ? `Showing ${offset+1}–${Math.min(offset+pageSize,total)} of ${fmt.format(total)} members` : '0 members';
  $('previous').disabled = offset === 0; $('next').disabled = offset + pageSize >= total;
}
async function loadQueue() {
  const request = ++queueRequest;
  const data = await api(`/admin/verification-queue?status=${$('queue-filter').value}&limit=${pageSize}&offset=${queueOffset}`);
  if (request !== queueRequest || !currentAdmin) return;
  queueTotal = data.total;
  if (queueOffset && queueOffset >= queueTotal) { queueOffset = Math.max(0, Math.floor((queueTotal-1)/pageSize)*pageSize); return loadQueue(); }
  const cards = data.verifications.map(item => {
    const card = node('article', undefined, 'verification'); const details = node('div');
    details.append(node('h3', `${item.skill_type.replaceAll('_',' ')} · ${item.user_name || item.user_email || 'Community member'}`), node('p', `${item.user_email || item.user_phone || item.user_id} · Submitted ${date(item.submitted_at)}`));
    if (item.notes) details.append(node('p', item.notes));
    if (item.rejection_reason) details.append(node('p', `Reason: ${item.rejection_reason}`));
    try {
      const url = new URL(item.certificate_url, location.origin);
      if ((url.origin === location.origin && url.pathname.startsWith('/uploads/')) || url.protocol === 'https:') {
        const link = node('a', 'View certificate ↗'); link.href = url.href; link.target = '_blank'; link.rel = 'noopener noreferrer'; details.append(link);
      }
    } catch (_) { /* Invalid links remain plain data. */ }
    const controls = node('div', undefined, 'verification-actions');
    controls.append(tag(item.status, item.status === 'APPROVED' ? 'online' : ''));
    if (item.status === 'PENDING') controls.append(action('Approve', 'approve', item.id), action('Reject', 'reject', item.id));
    card.append(details,controls); return card;
  });
  $('queue-list').replaceChildren(...(cards.length ? cards : [node('div', 'No verification requests in this queue.', 'empty')]));
  $('queue-page-label').textContent = queueTotal ? `${queueOffset+1}–${Math.min(queueOffset+pageSize,queueTotal)} of ${queueTotal} requests` : '0 requests';
  $('queue-previous').disabled = queueOffset === 0; $('queue-next').disabled = queueOffset + pageSize >= queueTotal;
}
async function sync() {
  if (syncing || !currentAdmin) return;
  syncing = true; $('refresh').disabled = true;
  try {
    await Promise.all([loadStats(), tab === 'users' ? loadUsers() : loadQueue()]);
    $('sync-status').replaceChildren(node('span', undefined, 'pulse'), node('span', `Updated ${new Date().toLocaleTimeString([], {hour:'2-digit',minute:'2-digit',second:'2-digit'})}`));
  } catch (error) { $('sync-status').textContent = 'Update failed · retrying'; showNotice(error.message); }
  finally { syncing = false; $('refresh').disabled = false; }
}
$('refresh').addEventListener('click', sync);
async function reloadView() { try { await (tab === 'users' ? loadUsers() : loadQueue()); } catch (error) { showNotice(error.message); } }
function changeTab(nextTab) {
  tab = nextTab; $('users-section').hidden = tab !== 'users'; $('queue-section').hidden = tab !== 'queue';
  $('users-tab').classList.toggle('selected', tab === 'users'); $('queue-tab').classList.toggle('selected', tab === 'queue'); reloadView();
}
$('users-tab').addEventListener('click', () => changeTab('users'));
$('queue-tab').addEventListener('click', () => changeTab('queue'));
$('search').addEventListener('input', () => { clearTimeout(searchTimer); searchTimer = setTimeout(() => { offset = 0; reloadView(); }, 250); });
for (const id of ['state-filter','role-filter','type-filter']) $(id).addEventListener('change', () => {offset = 0; reloadView();});
$('purge-demo-btn').addEventListener('click', () => {
  confirmAction('Clean demo logins?', 'This will permanently remove all temporary demo and anonymous victim accounts from the database. Real registered users and administrators will not be affected.', 'Clean demo logins', async () => {
    const res = await api('/admin/maintenance/purge-demo-users', {method:'POST'});
    showNotice(res?.message || 'Demo logins cleaned successfully.');
  });
});
$('queue-filter').addEventListener('change', () => {queueOffset = 0; reloadView();});
$('previous').addEventListener('click', () => { offset = Math.max(0,offset-pageSize); reloadView(); });
$('next').addEventListener('click', () => { offset += pageSize; reloadView(); });
$('queue-previous').addEventListener('click', () => { queueOffset = Math.max(0,queueOffset-pageSize); reloadView(); });
$('queue-next').addEventListener('click', () => { queueOffset += pageSize; reloadView(); });
function openUser(user) {
  $('user-form').reset(); $('user-form-error').textContent = '';
  $('user-dialog-title').textContent = user ? 'Edit user' : 'Add user';
  $('edit-id').value = user?.id || ''; $('edit-name').value = user?.name || '';
  $('edit-email').value = user?.email || ''; $('edit-email').required = !user || user.auth_provider === 'email';
  $('edit-phone').value = user?.phone || ''; $('edit-active').checked = user ? user.is_active : true;
  $('edit-role').value = String(user?.is_superuser || false); $('edit-trust').value = user?.trust_score ?? 50;
  $('edit-password').required = !user; $('password-label').hidden = !!user && user.auth_provider !== 'email';
  $('password-hint').textContent = user ? 'Leave blank to keep current password' : 'At least 8 characters';
  const self = user?.id === currentAdmin.id; $('edit-active').disabled = self; $('edit-role').disabled = self;
  $('user-dialog').showModal();
}
$('add-user').addEventListener('click', () => openUser(null));
for (const button of document.querySelectorAll('.close-dialog')) button.addEventListener('click', () => button.closest('dialog').close());
$('user-form').addEventListener('submit', async event => {
  event.preventDefault(); const button = event.submitter; button.disabled = true; $('user-form-error').textContent = '';
  const id = $('edit-id').value;
  const body = {name:$('edit-name').value.trim(), email:$('edit-email').value.trim() || null, phone:$('edit-phone').value.trim() || null, is_active:$('edit-active').checked, is_superuser:$('edit-role').value === 'true', trust_score:Number($('edit-trust').value)};
  if ($('edit-password').value) body.password = $('edit-password').value;
  try {
    const user = await api(id ? `/admin/users/${id}` : '/admin/users', {method:id ? 'PATCH' : 'POST', body:JSON.stringify(body)});
    if (user.id === currentAdmin.id) {currentAdmin = user; $('admin-name').textContent = user.name;}
    $('user-dialog').close(); $('user-form').reset(); showNotice(id ? 'Account updated.' : 'User added to your community.'); await sync();
  } catch (error) { $('user-form-error').textContent = error.message; }
  finally { button.disabled = false; }
});
function confirmAction(title, message, label, operation, rejection = false) {
  $('confirm-title').textContent = title; $('confirm-message').textContent = message;
  $('confirm-submit').textContent = label; $('confirm-error').textContent = '';
  $('review-label').hidden = !rejection; $('review-reason').required = rejection; $('review-reason').value = '';
  pendingAction = operation; $('confirm-dialog').showModal();
}
$('confirm-form').addEventListener('submit', async event => {
  event.preventDefault(); const button = event.submitter; button.disabled = true;
  try { await pendingAction(); $('confirm-dialog').close(); showNotice('Change saved.'); await sync(); }
  catch (error) { $('confirm-error').textContent = error.message; }
  finally { button.disabled = false; }
});
$('user-rows').addEventListener('click', event => {
  const button = event.target.closest('button[data-action]'); if (!button) return;
  const user = users.find(item => item.id === button.dataset.id); if (!user) return;
  const name = user.name || user.email || 'this user';
  if (button.dataset.action === 'edit') return openUser(user);
  if (button.dataset.action === 'delete') confirmAction('Delete account?', `Permanently delete ${name}? Their profile, skill submissions, and response engagements will be removed. Emergency history is retained without their account link. External Google/Firebase identities remain and may register again; suspend the account to block access instead. This cannot be undone.`, 'Delete permanently', () => api(`/admin/users/${user.id}`, {method:'DELETE'}));
  else confirmAction(user.is_active ? 'Suspend account?' : 'Enable account?', `${name} will ${user.is_active ? 'lose access to authenticated services' : 'be able to sign in again'}.`, user.is_active ? 'Suspend account' : 'Enable account', () => api(`/admin/users/${user.id}`, {method:'PATCH', body:JSON.stringify({is_active:!user.is_active})}));
});
$('queue-list').addEventListener('click', event => {
  const button = event.target.closest('button[data-action]'); if (!button) return;
  const approve = button.dataset.action === 'approve';
  confirmAction(approve ? 'Approve credential?' : 'Reject credential?', approve ? 'This awards the corresponding verified skill and updates the member’s trust score.' : 'Provide feedback to help the member correct their submission.', approve ? 'Approve credential' : 'Reject credential', () => api(`/admin/verification-queue/${button.dataset.id}/${approve ? 'approve' : 'reject'}`, {method:'POST', body:JSON.stringify(approve ? {} : {rejection_reason:$('review-reason').value.trim()})}), !approve);
});
