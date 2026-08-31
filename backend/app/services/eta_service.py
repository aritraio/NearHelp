"""NearHelp AI — Dynamic ETA Calculation & Geospatial Telemetry Service."""

import math


def haversine_distance(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """Calculate great-circle distance between two WGS84 GPS points in meters.
    
    Formula:
      a = sin²(Δφ/2) + cos φ1 ⋅ cos φ2 ⋅ sin²(Δλ/2)
      c = 2 ⋅ atan2( √a, √(1−a) )
      d = R ⋅ c
    Where R = 6,371,000 meters.
    """
    R = 6371000.0  # Earth mean radius in meters

    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    delta_phi = math.radians(lat2 - lat1)
    delta_lambda = math.radians(lon2 - lon1)

    a = math.sin(delta_phi / 2.0) ** 2 + math.cos(phi1) * math.cos(phi2) * (math.sin(delta_lambda / 2.0) ** 2)
    c = 2.0 * math.atan2(math.sqrt(a), math.sqrt(1.0 - a))
    return R * c


def calculate_bearing(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    """Calculate the forward initial compass bearing from (lat1, lon1) towards (lat2, lon2) in degrees [0, 360).
    
    Formula:
      θ = atan2( sin Δλ ⋅ cos φ2 , cos φ1 ⋅ sin φ2 − sin φ1 ⋅ cos φ2 ⋅ cos Δλ )
    """
    phi1 = math.radians(lat1)
    phi2 = math.radians(lat2)
    delta_lambda = math.radians(lon2 - lon1)

    y = math.sin(delta_lambda) * math.cos(phi2)
    x = math.cos(phi1) * math.sin(phi2) - math.sin(phi1) * math.cos(phi2) * math.cos(delta_lambda)
    bearing_rad = math.atan2(y, x)
    bearing_deg = (math.degrees(bearing_rad) + 360.0) % 360.0
    return round(bearing_deg, 1)


def bearing_to_compass(bearing_degrees: float) -> str:
    """Convert bearing angle in degrees into an 8-wind compass direction string."""
    directions = ["N", "NE", "E", "SE", "S", "SW", "W", "NW"]
    normalized = (bearing_degrees + 22.5) % 360.0
    index = int(normalized // 45.0)
    return directions[index % 8]


def format_distance(distance_meters: float) -> str:
    """Format distance in meters to a human-readable string (e.g. '340m', '1.2km')."""
    if distance_meters < 1000.0:
        return f"{int(round(distance_meters))}m"
    km = distance_meters / 1000.0
    return f"{km:.1f}km"


def format_eta(eta_minutes: float, distance_meters: float) -> str:
    """Format ETA in minutes to a human-readable display string."""
    if distance_meters <= 35.0:
        return "Arrived"
    if eta_minutes <= 0.0:
        return "Arrived"
    if eta_minutes < 1.0:
        seconds = max(15, int(round(eta_minutes * 60.0)))
        return f"{seconds} secs"
    if eta_minutes == int(eta_minutes):
        return f"{int(eta_minutes)} mins"
    return f"{eta_minutes:.1f} mins"


class ETAService:
    """Service providing high-accuracy urban ETA calculations and telemetry estimation."""

    # Urban routing tortuosity factor: accounts for street grid, intersections, and traffic density in Kolkata
    URBAN_TORTUOSITY_FACTOR = 1.35

    # Default speeds in meters per second
    DEFAULT_WALKING_SPEED_MPS = 1.25  # ~4.5 km/h
    DEFAULT_BIKE_SPEED_MPS = 5.55     # ~20.0 km/h
    DEFAULT_VEHICLE_SPEED_MPS = 8.33  # ~30.0 km/h

    ARRIVAL_THRESHOLD_METERS = 35.0

    @classmethod
    def calculate_eta(
        cls,
        responder_lat: float,
        responder_lon: float,
        target_lat: float,
        target_lon: float,
        speed_mps: float | None = None,
        travel_mode: str = "walking",
    ) -> tuple[float, float, str, float, str, str]:
        """Compute distance, bearing, and estimated travel time between responder and target incident.
        
        Returns:
            tuple of (
                distance_meters,
                eta_minutes,
                eta_formatted,
                bearing_deg,
                bearing_compass,
                distance_formatted
            )
        """
        distance_meters = haversine_distance(responder_lat, responder_lon, target_lat, target_lon)
        bearing_deg = calculate_bearing(responder_lat, responder_lon, target_lat, target_lon)
        bearing_compass = bearing_to_compass(bearing_deg)
        dist_str = format_distance(distance_meters)

        # If already arrived at target coordinates
        if distance_meters <= cls.ARRIVAL_THRESHOLD_METERS:
            return (
                round(distance_meters, 1),
                0.0,
                "Arrived",
                bearing_deg,
                bearing_compass,
                dist_str,
            )

        # Determine effective speed
        effective_speed = cls.DEFAULT_WALKING_SPEED_MPS
        if travel_mode == "bike":
            effective_speed = cls.DEFAULT_BIKE_SPEED_MPS
        elif travel_mode in ("vehicle", "ambulance", "car"):
            effective_speed = cls.DEFAULT_VEHICLE_SPEED_MPS

        # If live GPS sensor speed is provided and valid, blend with speed bounds
        if speed_mps is not None and speed_mps > 0.8:
            # Clamp reported speed between 1.0 m/s and 28.0 m/s (~100 km/h)
            effective_speed = max(1.0, min(float(speed_mps), 28.0))

        # Effective travel distance with urban tortuosity adjustment
        effective_distance = distance_meters * cls.URBAN_TORTUOSITY_FACTOR
        eta_seconds = effective_distance / effective_speed
        eta_minutes = round(eta_seconds / 60.0, 1)

        eta_str = format_eta(eta_minutes, distance_meters)

        return (
            round(distance_meters, 1),
            eta_minutes,
            eta_str,
            bearing_deg,
            bearing_compass,
            dist_str,
        )

    @classmethod
    def is_arrived(cls, distance_meters: float) -> bool:
        """Check if responder has arrived within proximity threshold of the victim."""
        return distance_meters <= cls.ARRIVAL_THRESHOLD_METERS


eta_service = ETAService()
