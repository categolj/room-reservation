import useSWR, { SWRConfiguration, useSWRConfig } from 'swr';
import { apiClient } from '../utils/apiClient';
import { Availability, Reservation } from '../types';

// Define fetcher keys for different endpoints
const availabilitiesKey = (date: string) => `/api/availabilities?date=${date}`;
const reservationsKey = (date: string, roomId: string) => `/api/reservations?date=${date}&roomId=${roomId}`;

// Generic fetcher function type for SWR
type Fetcher<T> = (url: string) => Promise<T>;

// SWR hooks for data fetching
export function useAvailabilities(date: string, config?: SWRConfiguration) {
    const fetcher: Fetcher<Availability[]> = (url) => {
        // Extract date from the URL to pass to the API client
        const dateParam = new URL(url, window.location.origin).searchParams.get('date') || date;
        return apiClient.getAvailabilities(dateParam);
    };

    return useSWR<Availability[], Error>(
        date ? availabilitiesKey(date) : null, 
        fetcher,
        config
    );
}

export function useReservations(date: string, roomId: string, config?: SWRConfiguration) {
    const fetcher: Fetcher<Reservation[]> = (url) => {
        // Extract date and roomId from the URL to pass to the API client
        const urlObj = new URL(url, window.location.origin);
        const dateParam = urlObj.searchParams.get('date') || date;
        const roomIdParam = urlObj.searchParams.get('roomId') || roomId;
        return apiClient.getReservations(dateParam, roomIdParam);
    };

    return useSWR<Reservation[], Error>(
        date && roomId ? reservationsKey(date, roomId) : null,
        fetcher,
        config
    );
}

// Mutation hooks for creating and deleting reservations
export function useReservationMutations() {
    const { mutate } = useSWRConfig();

    const createReservation = async (data: {
        roomId: string;
        date: string;
        startTime: string;
        endTime: string;
        purpose: string;
    }) => {
        await apiClient.createReservation(data);
        
        // Invalidate the affected data after mutation
        mutate(availabilitiesKey(data.date));
        mutate(reservationsKey(data.date, data.roomId));
    };

    const deleteReservation = async (reservationId: string, date: string, roomId: string) => {
        await apiClient.deleteReservation(reservationId);
        
        // Invalidate the affected data after mutation
        mutate(availabilitiesKey(date));
        mutate(reservationsKey(date, roomId));
    };

    return {
        createReservation,
        deleteReservation
    };
}
