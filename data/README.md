# NearHelp AI — Regional Data Directory

> **Maintainer**: Plaban Sarkar  
> **Last Updated**: November 2026  
> **Region**: Kolkata Metropolitan Area, West Bengal, India

This directory contains curated, structured geospatial datasets for emergency facilities across the Kolkata region. These datasets power the NearHelp AI **Smart SOS Engine** (Module 6) and the **Live Rescue Map** (Module 7), enabling proximity-based responder ranking and resource discovery.

---

## 📂 Directory Structure

```
data/
└── regional/
    ├── kolkata_facilities.json      # Hospitals & medical centres
    ├── kolkata_police.json          # Police stations & headquarters
    ├── kolkata_fire_stations.json   # Fire stations & brigades
    └── kolkata_blood_banks.json     # Blood banks & donation centres
```

---

## 📋 Dataset Overview

| File | Records | Facility Type | Coverage |
|:---|:---:|:---|:---|
| `kolkata_facilities.json` | 10+ | Hospitals / Medical Centres | Kolkata + Salt Lake |
| `kolkata_police.json` | 12 | Police Stations / HQ | Kolkata + Bidhannagar + Dum Dum |
| `kolkata_fire_stations.json` | 9 | Fire Stations / Brigade HQ | Kolkata + Salt Lake + Dum Dum |
| `kolkata_blood_banks.json` | 8 | Blood Banks | Kolkata + Salt Lake |

---

## 🏥 Schema Reference

All JSON files follow a consistent schema. Below is the full field reference per `facility_type`.

### Common Fields (All Facility Types)

| Field | Type | Description |
|:---|:---|:---|
| `name` | `string` | Official name of the facility |
| `facility_type` | `string` | One of: `hospital`, `police_station`, `fire_station`, `blood_bank` |
| `address` | `string` | Full street address |
| `phone` | `string` | Primary contact number (E.164 format) |
| `emergency_helpline` | `string` | Direct emergency line or national number (`100`, `101`) |
| `latitude` | `float` | WGS84 latitude (decimal degrees) |
| `longitude` | `float` | WGS84 longitude (decimal degrees) |
| `zone` | `string` | Neighbourhood / locality name |
| `district` | `string` | Administrative district |
| `is_24_hours` | `boolean` | Whether the facility operates around the clock |
| `is_verified` | `boolean` | Whether data has been manually verified |
| `metadata` | `object` | Facility-specific extended attributes (see below) |

---

### 🏨 Hospital / Medical Centre (`facility_type: "hospital"`)

| Field | Type | Description |
|:---|:---|:---|
| `bed_availability` | `int` | Current available general beds |
| `total_beds` | `int` | Total general bed capacity |
| `icu_availability` | `int` | Current available ICU beds |
| `total_icu` | `int` | Total ICU capacity |
| `trauma_level` | `string` | Trauma centre classification |
| `has_cardiac_unit` | `boolean` | Dedicated cardiac care unit present |
| `has_burn_unit` | `boolean` | Dedicated burn care unit present |
| `aed_building_name` | `string \| null` | Building name where AED is located |
| `aed_location_description` | `string \| null` | Specific AED location description |
| `aed_access_code` | `string \| null` | AED cabinet access code if applicable |
| **metadata** | | |
| `ambulance_count` | `int` | Number of ambulances on-site |
| `oxygen_capacity_liters` | `int` | On-site oxygen storage capacity (litres) |
| `blood_bank_on_site` | `boolean` | Blood bank within hospital premises |
| `nabh_accredited` | `boolean` | NABH accreditation status |

---

### 🚔 Police Station (`facility_type: "police_station"`)

| Field | Type | Description |
|:---|:---|:---|
| `police_zone` | `string` | Commissionerate or division name |
| `station_type` | `string` | One of: `headquarters`, `police_station`, `outpost` |
| `has_women_helpdesk` | `boolean` | Dedicated women's helpdesk present |
| `has_cyber_cell` | `boolean` | Cyber crime cell present |
| `has_pcr_van` | `boolean` | PCR (Police Control Room) van available |
| `jurisdiction_area_sq_km` | `float` | Jurisdiction area in square kilometres |
| **metadata** | | |
| `pcr_van_count` | `int` | Number of PCR vans |
| `personnel_strength` | `int` | Approximate active personnel count |
| `has_holding_cell` | `boolean` | Detention cell present |
| `has_control_room` | `boolean` | Local control room present |

---

### 🚒 Fire Station (`facility_type: "fire_station"`)

| Field | Type | Description |
|:---|:---|:---|
| `station_type` | `string` | One of: `headquarters`, `fire_station`, `sub_station` |
| `response_radius_km` | `float` | Typical first-response coverage radius (km) |
| **metadata** | | |
| `fire_engines` | `int` | Number of fire engines/tenders |
| `water_tankers` | `int` | Number of water tanker trucks |
| `rescue_vans` | `int` | Number of rescue/emergency vans |
| `aerial_platforms` | `int` | Number of aerial ladder/platform units |
| `personnel_strength` | `int` | Approximate active personnel count |
| `has_hazmat_unit` | `boolean` | Hazardous materials response team present |
| `has_rescue_unit` | `boolean` | Dedicated technical rescue unit present |
| `has_dive_team` | `boolean` | Water/dive rescue team present |

---

### 🩸 Blood Bank (`facility_type: "blood_bank"`)

| Field | Type | Description |
|:---|:---|:---|
| `parent_hospital` | `string` | Hospital the blood bank is affiliated with |
| `blood_groups_available` | `string[]` | List of blood groups routinely stocked |
| `has_apheresis_unit` | `boolean` | Apheresis (single-donor platelet) unit present |
| `has_component_separation` | `boolean` | Blood component separation capability |
| `operating_hours` | `string` | Hours of operation (if not 24×7) |
| **metadata** | | |
| `storage_capacity_units` | `int` | Maximum blood unit storage capacity |
| `daily_collection_avg` | `int` | Average daily donation collection (units) |
| `accepts_voluntary_donation` | `boolean` | Walk-in voluntary donation accepted |
| `mobile_blood_collection` | `boolean` | Mobile blood donation camps operated |
| `nabh_accredited` | `boolean` | NABH accreditation status |
| `naco_licensed` | `boolean` | Licensed by National AIDS Control Organisation |

---

## 🗺️ Coordinate Reference System

All coordinates use **WGS84** (EPSG:4326) decimal degree format, compatible with:
- Google Maps SDK (Android)
- PostGIS `geography` / `geometry` columns
- GeoJSON standard

---

## 📌 Data Sources & Accuracy Note

Data compiled from:
- Official West Bengal government facility registries
- Kolkata Police official website
- West Bengal Fire & Emergency Services
- NACO Blood Bank locator portal
- Direct facility verification

> **Note**: Bed availability and resource counts (`bed_availability`, `icu_availability`, `pcr_van_count`, etc.) represent typical operational figures and are **not real-time**. Live availability should be fetched via hospital APIs or direct contact. The NearHelp AI backend should treat these as baseline estimates for triage ranking only.

---

## ➕ Adding New Data

To add new facilities, follow the schema above and open a Pull Request targeting the `main` branch. Use the commit prefix:

```
feat(data): add <facility_type> dataset for <region>
```

Example:
```
feat(data): add Kolkata blood banks regional dataset
```
