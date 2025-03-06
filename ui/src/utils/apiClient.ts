import { Availability, ApiError, Reservation } from '../types';

interface ReservationData {
    roomId: string;
    date: string;
    startTime: string;
    endTime: string;
    purpose: string;
}

class ApiClient {
    private async handleResponse<T>(response: Response): Promise<T> {
        if (!response.ok) {
            const errorData: ApiError = await response.json();
            throw new Error(errorData.message);
        }

        // For void responses (no content)
        if (response.status === 204 || response.status === 201 || response.headers.get("content-length") === "0") {
            return undefined as T;
        }

        return response.json();
    }

    async getAvailabilities(date: string): Promise<Availability[]> {
        const response = await fetch(`/api/availabilities?date=${date}`);
        return this.handleResponse<Availability[]>(response);
    }

    async getReservations(date: string, roomId: string): Promise<Reservation[]> {
        const response = await fetch(`/api/reservations?date=${date}&roomId=${roomId}`);
        return this.handleResponse<Reservation[]>(response);
    }

    async createReservation(data: ReservationData): Promise<void> {
        const response = await fetch('/api/reservations', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        });
        return this.handleResponse<void>(response);
    }

    async deleteReservation(reservationId: string): Promise<void> {
        const response = await fetch(`/api/reservations/${reservationId}`, {
            method: 'DELETE',
        });
        return this.handleResponse<void>(response);
    }
}

export const apiClient = new ApiClient();
