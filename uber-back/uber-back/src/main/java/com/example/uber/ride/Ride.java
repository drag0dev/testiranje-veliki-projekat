package com.example.uber.ride;

import com.example.uber.user.User;
import com.example.uber.vehicle.VehicleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "rides")
@Getter
@Setter
@NoArgsConstructor
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "vehicle_type", nullable = false, columnDefinition = "vehicle_type")
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "ride_status")
    private RideStatus status;

    @Column(name = "start_address", nullable = false, length = 255)
    private String startAddress;

    @Column(name = "start_lat", precision = 9, scale = 6)
    private BigDecimal startLat;

    @Column(name = "start_lng", precision = 9, scale = 6)
    private BigDecimal startLng;

    @Column(name = "end_address", nullable = false, length = 255)
    private String endAddress;

    @Column(name = "end_lat", precision = 9, scale = 6)
    private BigDecimal endLat;

    @Column(name = "end_lng", precision = 9, scale = 6)
    private BigDecimal endLng;

    @Column(name = "distance_km", precision = 6, scale = 2)
    private BigDecimal distanceKm;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "baby_transport", nullable = false)
    private boolean babyTransport;

    @Column(name = "pet_transport", nullable = false)
    private boolean petTransport;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @ManyToOne
    @JoinColumn(name = "cancelled_by")
    private User cancelledBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
