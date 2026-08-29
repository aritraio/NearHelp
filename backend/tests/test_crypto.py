"""NearHelp AI — Unit Tests for AES-256-GCM Medical Data Encryption."""

from app.core.crypto import CIPHERTEXT_PREFIX, MedicalDataEncryption


def test_encrypt_and_decrypt_list():
    """Test encrypting and decrypting a list of medical conditions."""
    conditions = ["Type 1 Diabetes", "Asthma", "Cardiac Arrhythmia"]
    encrypted = MedicalDataEncryption.encrypt(conditions)

    assert encrypted is not None
    assert encrypted.startswith(CIPHERTEXT_PREFIX)
    assert "Diabetes" not in encrypted  # Plaintext is not exposed

    decrypted = MedicalDataEncryption.decrypt(encrypted)
    assert decrypted == conditions


def test_encrypt_and_decrypt_allergies():
    """Test encrypting and decrypting known allergies."""
    allergies = ["Penicillin", "Peanuts", "Latex", "Sulfa Drugs"]
    encrypted = MedicalDataEncryption.encrypt(allergies)

    assert encrypted.startswith(CIPHERTEXT_PREFIX)
    assert "Penicillin" not in encrypted

    decrypted = MedicalDataEncryption.decrypt(encrypted)
    assert decrypted == allergies


def test_encrypt_and_decrypt_dict():
    """Test encrypting and decrypting complex dict objects."""
    data = {
        "pacemaker": True,
        "implant_model": "Medtronic Azure XT",
        "doctor_phone": "+919830012345",
    }
    encrypted = MedicalDataEncryption.encrypt(data)
    assert encrypted.startswith(CIPHERTEXT_PREFIX)

    decrypted = MedicalDataEncryption.decrypt(encrypted)
    assert decrypted == data


def test_encrypt_none():
    """Test encrypting None returns None."""
    assert MedicalDataEncryption.encrypt(None) is None
    assert MedicalDataEncryption.decrypt(None) is None


def test_decrypt_plaintext_fallback():
    """Test decrypting unencrypted string or list falls back gracefully."""
    raw_list = ["Asthma", "Hypertension"]
    assert MedicalDataEncryption.decrypt(raw_list) == raw_list

    raw_json = '["Asthma", "Hypertension"]'
    assert MedicalDataEncryption.decrypt(raw_json) == raw_list

    plain_text = "Standard note"
    assert MedicalDataEncryption.decrypt(plain_text) == plain_text


def test_tampered_ciphertext_fails_gracefully():
    """Test that tampered ciphertext does not throw unhandled exceptions."""
    conditions = ["Asthma"]
    encrypted = MedicalDataEncryption.encrypt(conditions)

    # Tamper with the base64 characters
    tampered = encrypted[:-4] + "AAAA"
    decrypted = MedicalDataEncryption.decrypt(tampered)
    # Should safely return tampered string without crashing
    assert decrypted == tampered
