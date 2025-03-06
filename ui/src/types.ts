export interface Availability {
    roomId: string;
    roomName: string;
    date: string;
    available: boolean;
}

export interface Reservation {
    reservationId: string;
    roomId: string;
    date: string;
    startTime: string;
    endTime: string;
    purpose: string;
}

export interface ReservationFormData {
    startTime: string;
    endTime: string;
    purpose: string;
}

export interface ApiError {
    message: string;
}
