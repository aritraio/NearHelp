"""NearHelp AI — User Profile & Encrypted Medical ID Business Logic Service."""

import logging
import os
import uuid
from typing import Any

from fastapi import HTTPException, UploadFile, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.crypto import MedicalDataEncryption
from app.models.user import User
from app.schemas.user import (
    EmergencyContactCreateSchema,
    EmergencyContactSchema,
    EmergencyContactUpdateSchema,
    MedicalIDResponse,
    MedicalIDUpdateRequest,
    UserResponse,
    UserUpdateRequest,
)

logger = logging.getLogger(__name__)

# Valid ISO 639-1 language code subset supported by NearHelp
ALLOWED_LANGUAGES = {"en", "bn", "hi", "es", "fr", "ar", "de", "zh", "ja", "ta", "te", "mr", "gu", "ur"}
ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/webp", "image/jpg"}
MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024  # 5 MB
MAX_EMERGENCY_CONTACTS = 5
UPLOAD_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../uploads/avatars"))


class UserService:
    """Service encapsulating user profile operations, AES-256 encrypted medical ID, and emergency contacts."""

    @classmethod
    def format_user_response(cls, user: User) -> UserResponse:
        """Construct UserResponse DTO with decrypted medical data and typed contacts."""
        # Decrypt medical conditions list
        decrypted_conditions = MedicalDataEncryption.decrypt(user.medical_conditions)
        if isinstance(decrypted_conditions, list):
            conditions_list = [str(c) for c in decrypted_conditions]
        elif isinstance(decrypted_conditions, str) and decrypted_conditions:
            conditions_list = [decrypted_conditions]
        else:
            conditions_list = []

        # Decrypt known allergies list
        decrypted_allergies = MedicalDataEncryption.decrypt(user.known_allergies)
        if isinstance(decrypted_allergies, list):
            allergies_list = [str(a) for a in decrypted_allergies]
        elif isinstance(decrypted_allergies, str) and decrypted_allergies:
            allergies_list = [decrypted_allergies]
        else:
            allergies_list = []

        # Decrypt medical notes
        decrypted_notes = MedicalDataEncryption.decrypt(user.medical_notes)
        notes_str = str(decrypted_notes) if decrypted_notes is not None else None

        # Parse emergency contacts safely
        contacts_raw = user.emergency_contacts or []
        contacts_parsed = []
        for c in contacts_raw:
            if isinstance(c, dict):
                if "id" not in c or not c["id"]:
                    c["id"] = str(uuid.uuid4())
                contacts_parsed.append(EmergencyContactSchema(**c))

        return UserResponse(
            id=user.id,
            email=user.email,
            name=user.name,
            phone=user.phone,
            photo_url=user.photo_url,
            blood_group=user.blood_group,
            phone_verified=user.phone_verified,
            auth_provider=user.auth_provider,
            is_anonymous=user.is_anonymous,
            is_active=user.is_active,
            is_superuser=user.is_superuser,
            has_pacemaker=getattr(user, "has_pacemaker", False) or False,
            is_organ_donor=getattr(user, "is_organ_donor", False) or False,
            medical_notes=notes_str,
            medical_conditions=conditions_list,
            known_allergies=allergies_list,
            emergency_contacts=contacts_parsed,
            languages=user.languages or ["en"],
            skills=user.skills or [],
            trust_score=user.trust_score or 50.0,
            badges=user.badges or [],
            fcm_token=user.fcm_token,
            created_at=user.created_at,
            updated_at=user.updated_at,
            last_login_at=user.last_login_at,
        )

    @classmethod
    async def update_profile(
        cls, db: AsyncSession, user: User, req: UserUpdateRequest
    ) -> UserResponse:
        """Update profile fields with automatic encryption for sensitive health items."""
        if req.name is not None:
            user.name = req.name.strip()
        if req.phone is not None:
            user.phone = req.phone.strip()
        if req.photo_url is not None:
            user.photo_url = req.photo_url.strip()
        if req.blood_group is not None:
            user.blood_group = req.blood_group.strip()
        if req.languages is not None:
            cls.validate_languages(req.languages)
            user.languages = req.languages
        if req.has_pacemaker is not None:
            user.has_pacemaker = req.has_pacemaker
        if req.is_organ_donor is not None:
            user.is_organ_donor = req.is_organ_donor

        # Encrypt medical conditions at rest
        if req.medical_conditions is not None:
            user.medical_conditions = MedicalDataEncryption.encrypt(req.medical_conditions)

        # Encrypt known allergies at rest
        if req.known_allergies is not None:
            user.known_allergies = MedicalDataEncryption.encrypt(req.known_allergies)

        # Encrypt medical notes at rest
        if req.medical_notes is not None:
            user.medical_notes = MedicalDataEncryption.encrypt(req.medical_notes)

        # Process emergency contacts
        if req.emergency_contacts is not None:
            if len(req.emergency_contacts) > MAX_EMERGENCY_CONTACTS:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"Cannot configure more than {MAX_EMERGENCY_CONTACTS} emergency contacts.",
                )
            contacts_list = []
            for c in req.emergency_contacts:
                c_dict = c.model_dump()
                if not c_dict.get("id"):
                    c_dict["id"] = str(uuid.uuid4())
                contacts_list.append(c_dict)
            user.emergency_contacts = contacts_list

        await db.commit()
        await db.refresh(user)
        return cls.format_user_response(user)

    @classmethod
    def get_medical_id(cls, user: User) -> MedicalIDResponse:
        """Retrieve the user's decrypted Medical ID Card."""
        profile = cls.format_user_response(user)
        return MedicalIDResponse(
            blood_group=profile.blood_group,
            medical_conditions=profile.medical_conditions,
            known_allergies=profile.known_allergies,
            has_pacemaker=profile.has_pacemaker,
            is_organ_donor=profile.is_organ_donor,
            medical_notes=profile.medical_notes,
            emergency_contacts=profile.emergency_contacts,
            is_encrypted_at_rest=True,
        )

    @classmethod
    async def update_medical_id(
        cls, db: AsyncSession, user: User, req: MedicalIDUpdateRequest
    ) -> MedicalIDResponse:
        """Update Medical ID attributes with AES-256-GCM encryption at rest."""
        if req.blood_group is not None:
            user.blood_group = req.blood_group.strip()
        if req.medical_conditions is not None:
            user.medical_conditions = MedicalDataEncryption.encrypt(req.medical_conditions)
        if req.known_allergies is not None:
            user.known_allergies = MedicalDataEncryption.encrypt(req.known_allergies)
        if req.has_pacemaker is not None:
            user.has_pacemaker = req.has_pacemaker
        if req.is_organ_donor is not None:
            user.is_organ_donor = req.is_organ_donor
        if req.medical_notes is not None:
            user.medical_notes = MedicalDataEncryption.encrypt(req.medical_notes)

        await db.commit()
        await db.refresh(user)
        return cls.get_medical_id(user)

    # --------------------------------------------------------------------------
    # Emergency Contacts Sub-Resource
    # --------------------------------------------------------------------------
    @classmethod
    def list_contacts(cls, user: User) -> list[EmergencyContactSchema]:
        """List all registered emergency contacts (up to 5)."""
        raw_contacts = user.emergency_contacts or []
        parsed = []
        for c in raw_contacts:
            if isinstance(c, dict):
                if not c.get("id"):
                    c["id"] = str(uuid.uuid4())
                parsed.append(EmergencyContactSchema(**c))
        return parsed

    @classmethod
    async def add_contact(
        cls, db: AsyncSession, user: User, req: EmergencyContactCreateSchema
    ) -> EmergencyContactSchema:
        """Add a new emergency contact enforcing the 5-contact maximum limit."""
        contacts = list(user.emergency_contacts or [])
        if len(contacts) >= MAX_EMERGENCY_CONTACTS:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Emergency contacts limit reached (maximum {MAX_EMERGENCY_CONTACTS} contacts).",
            )

        # If marked primary, unset other contacts' primary flag
        if req.is_primary:
            for c in contacts:
                c["is_primary"] = False

        new_contact = {
            "id": str(uuid.uuid4()),
            "name": req.name.strip(),
            "phone": req.phone.strip(),
            "relationship": req.relationship.strip(),
            "is_primary": req.is_primary if len(contacts) > 0 else True,  # First contact is primary by default
        }
        contacts.append(new_contact)
        user.emergency_contacts = contacts

        await db.commit()
        await db.refresh(user)
        return EmergencyContactSchema(**new_contact)

    @classmethod
    async def update_contact(
        cls,
        db: AsyncSession,
        user: User,
        contact_id: str,
        req: EmergencyContactUpdateSchema,
    ) -> EmergencyContactSchema:
        """Update an existing emergency contact by unique ID."""
        contacts = list(user.emergency_contacts or [])
        found_idx = None
        for i, c in enumerate(contacts):
            if c.get("id") == contact_id:
                found_idx = i
                break

        if found_idx is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Emergency contact with id '{contact_id}' not found.",
            )

        target = dict(contacts[found_idx])
        if req.name is not None:
            target["name"] = req.name.strip()
        if req.phone is not None:
            target["phone"] = req.phone.strip()
        if req.relationship is not None:
            target["relationship"] = req.relationship.strip()
        if req.is_primary is not None:
            target["is_primary"] = req.is_primary
            if req.is_primary:
                for j, c in enumerate(contacts):
                    if j != found_idx:
                        c["is_primary"] = False

        contacts[found_idx] = target
        user.emergency_contacts = contacts

        await db.commit()
        await db.refresh(user)
        return EmergencyContactSchema(**target)

    @classmethod
    async def delete_contact(cls, db: AsyncSession, user: User, contact_id: str) -> bool:
        """Remove an emergency contact by unique ID."""
        contacts = list(user.emergency_contacts or [])
        original_count = len(contacts)
        contacts = [c for c in contacts if c.get("id") != contact_id]

        if len(contacts) == original_count:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Emergency contact with id '{contact_id}' not found.",
            )

        # If we deleted the primary contact, assign primary to the first remaining contact
        if contacts and not any(c.get("is_primary") for c in contacts):
            contacts[0]["is_primary"] = True

        user.emergency_contacts = contacts
        await db.commit()
        await db.refresh(user)
        return True

    # --------------------------------------------------------------------------
    # Profile Photo & Language Preferences
    # --------------------------------------------------------------------------
    @classmethod
    async def save_profile_photo(cls, db: AsyncSession, user: User, file: UploadFile) -> str:
        """Validate and save user profile picture locally in upload store."""
        if file.content_type not in ALLOWED_IMAGE_TYPES:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Unsupported image format '{file.content_type}'. Allowed: {list(ALLOWED_IMAGE_TYPES)}",
            )

        content = await file.read()
        if len(content) > MAX_IMAGE_SIZE_BYTES:
            raise HTTPException(
                status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
                detail="Profile image exceeds maximum size limit of 5 MB.",
            )

        os.makedirs(UPLOAD_DIR, exist_ok=True)
        ext = file.filename.split(".")[-1].lower() if file.filename and "." in file.filename else "jpg"
        filename = f"{user.id}_{uuid.uuid4().hex[:8]}.{ext}"
        filepath = os.path.join(UPLOAD_DIR, filename)

        with open(filepath, "wb") as f:
            f.write(content)

        photo_url = f"/uploads/avatars/{filename}"
        user.photo_url = photo_url
        await db.commit()
        await db.refresh(user)
        return photo_url

    @classmethod
    def validate_languages(cls, languages: list[str]) -> None:
        """Validate ISO 639-1 language codes."""
        if not languages:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="At least one language must be specified.",
            )
        for code in languages:
            clean = code.strip().lower()
            if clean not in ALLOWED_LANGUAGES and len(clean) != 2:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"Invalid ISO 639-1 language code '{code}'.",
                )
