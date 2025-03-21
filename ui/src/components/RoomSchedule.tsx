import {useEffect, useRef, useState} from 'react';
import {apiClient} from '../utils/apiClient';
import {useNavigate, useParams} from 'react-router-dom';
import {convertTimeFormat, fromDateString, toDateString} from '../utils/dateUtils';
import {Card} from 'primereact/card';
import {Toast} from 'primereact/toast';
import {InputText} from 'primereact/inputtext';
import {Button} from 'primereact/button';
import {Dialog} from 'primereact/dialog';
import {Dropdown} from 'primereact/dropdown';
import {ProgressSpinner} from 'primereact/progressspinner';
import {Reservation, ReservationFormData} from '../types';
import styles from './RoomSchedule.module.css';

// Wrapper component for parameter validation
export const RoomSchedule = () => {
    const {roomId, date} = useParams<{ roomId: string; date: string }>();
    const navigate = useNavigate();

    if (!roomId || !date) {
        navigate('/');
        return null;
    }

    // After validation, we can safely use these parameters
    return <RoomScheduleContent roomId={roomId} date={date}/>;
};

// Main component with validated parameters
const RoomScheduleContent = ({roomId, date}: { roomId: string; date: string }) => {
    const navigate = useNavigate();
    const [roomName, setRoomName] = useState<string>("");
    const [reservations, setReservations] = useState<Reservation[]>([]);
    const [loading, setLoading] = useState(false);
    const [isDragging, setIsDragging] = useState(false);
    const [dragStart, setDragStart] = useState<number | null>(null);
    const [dragEnd, setDragEnd] = useState<number | null>(null);
    const scheduleRef = useRef<HTMLDivElement>(null);
    const [formData, setFormData] = useState<ReservationFormData>({
        startTime: '',
        endTime: '',
        purpose: ''
    });
    const [error, setError] = useState<string | null>(null);
    const [selectedReservation, setSelectedReservation] = useState<Reservation | null>(null);
    const [dialogVisible, setDialogVisible] = useState(false);
    const toast = useRef<Toast>(null);
    const purposeInputRef = useRef<HTMLInputElement>(null);

    const timeOptions = (() => {
        const options = [];
        for (let hour = 0; hour < 24; hour++) {
            const hourStr = hour.toString().padStart(2, '0');
            options.push({label: `${hourStr}:00`, value: `${hourStr}:00`});
            options.push({label: `${hourStr}:30`, value: `${hourStr}:30`});
        }
        return options;
    })();

    const reloadReservations = async () => {
        const reservations = await apiClient.getReservations(date, roomId);
        setReservations(reservations);
    };

    const handleDateChange = (offset: number) => {
        const currentDate = fromDateString(date);
        currentDate.setDate(currentDate.getDate() + offset);
        navigate(`/${toDateString(currentDate)}/rooms/${roomId}`);
    };

    const handleReservationClick = (reservation: Reservation) => {
        setSelectedReservation(reservation);
        setDialogVisible(true);
    };

    const handleCancelReservation = async () => {
        if (!selectedReservation) return;

        try {
            await apiClient.deleteReservation(selectedReservation.reservationId);

            toast.current?.show({
                severity: 'success',
                summary: 'Success',
                detail: 'Reservation cancelled successfully',
                life: 3000
            });

            setDialogVisible(false);
            setSelectedReservation(null);
            reloadReservations();
            setTimeout(reloadReservations, 1000);
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: 'Error',
                detail: err instanceof Error ? err.message : 'An error occurred',
                life: 3000
            });
        }
    };

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            try {
                const availabilities = await apiClient.getAvailabilities(date);
                const room = availabilities.find(a => a.roomId === roomId);
                if (room) {
                    setRoomName(room.roomName);
                } else {
                    throw new Error('Room not found');
                }
                reloadReservations();
            } catch (err) {
                toast.current?.show({
                    severity: 'error',
                    summary: 'Error',
                    detail: err instanceof Error ? err.message : 'An error occurred',
                    life: 3000
                });
                setRoomName("");
                setReservations([]);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [date, roomId]);

    const validateForm = (data: ReservationFormData): string | null => {
        if (!data.startTime || !data.endTime) {
            return 'Start time and end time are required';
        }

        if (!data.purpose) {
            return 'Purpose is required';
        }

        const [startHour, startMinute] = data.startTime.split(':').map(Number);
        const [endHour, endMinute] = data.endTime.split(':').map(Number);

        const startMinutes = startHour * 60 + startMinute;
        const endMinutes = endHour * 60 + endMinute;

        if (startMinutes >= endMinutes) {
            return 'End time must be after start time';
        }

        return null;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        // Validate form
        const validationError = validateForm(formData);
        if (validationError) {
            setError(validationError);
            return;
        }

        try {
            await apiClient.createReservation({
                roomId: roomId,
                date: date,
                startTime: formData.startTime,
                endTime: formData.endTime,
                purpose: formData.purpose
            });

            toast.current?.show({
                severity: 'success',
                summary: 'Success',
                detail: 'Reservation created successfully',
                life: 3000
            });

            setFormData({startTime: '', endTime: '', purpose: ''});
            reloadReservations();
            setTimeout(reloadReservations, 1000);
        } catch (err) {
            toast.current?.show({
                severity: 'error',
                summary: 'Error',
                detail: err instanceof Error ? err.message : 'An error occurred',
                life: 3000
            });
        }
    };

    const timeSlots = Array.from({length: 24}, (_, i) => `${i.toString().padStart(2, '0')}:00`);

    const calculateTimeFromY = (y: number): string => {
        if (!scheduleRef.current) return '00:00';
        const rect = scheduleRef.current.getBoundingClientRect();
        const relativeY = y - rect.top;
        const percentage = (relativeY / rect.height) * 100;
        const hour = Math.floor((percentage * 24) / 100);
        return `${hour.toString().padStart(2, '0')}:00`;
    };

    const handleMouseDown = (e: React.MouseEvent) => {
        if (!scheduleRef.current) return;

        // Check if clicking on a reservation
        const target = e.target as HTMLElement;
        if (target.closest(`.${styles.reservation}`)) {
            return;
        }

        const startY = e.clientY;
        setIsDragging(true);
        setDragStart(startY);
        setDragEnd(startY);
    };

    const handleMouseMove = (e: React.MouseEvent) => {
        if (!isDragging) return;
        setDragEnd(e.clientY);
    };

    const handleMouseUp = () => {
        if (!isDragging || dragStart === null || dragEnd === null) return;

        const startTime = calculateTimeFromY(Math.min(dragStart, dragEnd));
        const endTime = calculateTimeFromY(Math.max(dragStart, dragEnd));

        setFormData({
            ...formData,
            startTime: startTime,
            endTime: endTime
        });

        // Focus the purpose input after setting the time
        setTimeout(() => {
            purposeInputRef.current?.focus();
        }, 0);

        setIsDragging(false);
        setDragStart(null);
        setDragEnd(null);
    };
    return (
        <Card className="mt-4">
            <Toast ref={toast}/>
            {loading && (
                <div className="flex justify-content-center mt-4">
                    <ProgressSpinner
                        style={{width: '50px', height: '50px'}}
                        strokeWidth="4"
                        animationDuration=".5s"
                    />
                </div>
            )}
            <Dialog
                visible={dialogVisible}
                onHide={() => setDialogVisible(false)}
                header="Reservation Details"
                modal
                style={{width: '50vw'}}
            >
                {selectedReservation && (
                    <div className="flex flex-column gap-3">
                        <div>
                            <strong>Time:</strong> {convertTimeFormat(
                            selectedReservation.startTime)} - {convertTimeFormat(
                            selectedReservation.endTime)}
                        </div>
                        {selectedReservation.purpose && (
                            <div>
                                <strong>Purpose:</strong> {selectedReservation.purpose}
                            </div>
                        )}
                        <div className="flex justify-content-end">
                            <Button
                                label="Cancel Reservation"
                                severity="danger"
                                onClick={handleCancelReservation}
                            />
                        </div>
                    </div>
                )}
            </Dialog>
            <div className="grid">
                <div className="col-8">
                    <div className={styles.roomTitle}>
                        <h2 className={styles.roomName}>{roomName}</h2>
                        <div className={styles.scheduleDate}>
                            <button
                                className={styles.dateNavButton}
                                onClick={() => handleDateChange(-1)}
                                aria-label="Previous day"
                            >
                                ← Previous
                            </button>
                            <span>{date}</span>
                            <button
                                className={styles.dateNavButton}
                                onClick={() => handleDateChange(1)}
                                aria-label="Next day"
                            >
                                Next →
                            </button>
                        </div>
                    </div>
                    <div
                        className={styles.scheduleContainer}
                        ref={scheduleRef}
                        onMouseDown={handleMouseDown}
                        onMouseMove={handleMouseMove}
                        onMouseUp={handleMouseUp}
                        onMouseLeave={handleMouseUp}>
                        {timeSlots.map((time, index) => (
                            <div key={time}>
                                <div
                                    className={styles.timeSlot}
                                    style={{
                                        top: `${(index * 100) / 24}%`
                                    }}
                                >
                                    {time}
                                </div>
                                <div
                                    className={styles.timeSlotLine}
                                    style={{
                                        top: `${(index * 100) / 24}%`
                                    }}
                                />
                            </div>
                        ))}
                        {reservations.map((reservation, index) => (
                            <div
                                key={index}
                                className={styles.reservation}
                                style={{
                                    top: `${(parseInt(reservation.startTime.split(':')[0]) * 100)
                                    / 24}%`,
                                    height: `${((parseInt(reservation.endTime.split(':')[0])
                                        - parseInt(reservation.startTime.split(':')[0])) * 100)
                                    / 24}%`,
                                    cursor: 'pointer'
                                }}
                                onClick={() => handleReservationClick(reservation)}
                            >
                                {(() => {
                                    const startHour = parseInt(reservation.startTime.split(':')[0]);
                                    const endHour = parseInt(reservation.endTime.split(':')[0]);
                                    const duration = endHour - startHour;

                                    if (duration < 2) {
                                        return (
                                            <div className={styles.shortReservation}>
                                                <span>{convertTimeFormat(
                                                    reservation.startTime)} - {convertTimeFormat(
                                                    reservation.endTime)}</span>
                                                {reservation.purpose && (
                                                    <>
                                                        <span className={styles.separator}>|</span>
                                                        <span>{reservation.purpose}</span>
                                                    </>
                                                )}
                                            </div>
                                        );
                                    } else {
                                        return (
                                            <>
                                                <div className="text-sm">
                                                    {convertTimeFormat(
                                                        reservation.startTime)} - {convertTimeFormat(
                                                    reservation.endTime)}
                                                </div>
                                                {reservation.purpose && (
                                                    <div
                                                        className="text-sm text-gray-600 mt-1">{reservation.purpose}</div>
                                                )}
                                            </>
                                        );
                                    }
                                })()}
                            </div>
                        ))}
                        {isDragging && dragStart !== null && dragEnd !== null && (
                            <div
                                className={styles.dragSelection}
                                style={{
                                    top: `${(parseInt(
                                        calculateTimeFromY(Math.min(dragStart, dragEnd)).split(
                                            ':')[0]) * 100) / 24}%`,
                                    height: `${((parseInt(
                                        calculateTimeFromY(Math.max(dragStart, dragEnd)).split(
                                            ':')[0]) - parseInt(
                                        calculateTimeFromY(Math.min(dragStart, dragEnd)).split(
                                            ':')[0])) * 100) / 24}%`
                                }}
                            >
                                <div className={styles.timeGuide}>
                                    {calculateTimeFromY(
                                        Math.min(dragStart, dragEnd))} - {calculateTimeFromY(
                                    Math.max(dragStart, dragEnd))}
                                </div>
                            </div>
                        )}
                    </div>
                </div>
                <div className="col-4">
                    <form onSubmit={handleSubmit} className="flex flex-column gap-3">
                        <div>
                            <label htmlFor="startTime" className="block mb-2">Start Time</label>
                            <Dropdown
                                id="startTime"
                                value={formData.startTime}
                                options={timeOptions}
                                onChange={(e) => setFormData({...formData, startTime: e.value})}
                                className="w-full"
                                placeholder="Select start time"
                            />
                        </div>
                        <div>
                            <label htmlFor="endTime" className="block mb-2">End Time</label>
                            <Dropdown
                                id="endTime"
                                value={formData.endTime}
                                options={timeOptions}
                                onChange={(e) => setFormData({...formData, endTime: e.value})}
                                className="w-full"
                                placeholder="Select end time"
                            />
                        </div>
                        <div>
                            <label htmlFor="purpose" className="block mb-2">Purpose</label>
                            <InputText
                                id="purpose"
                                ref={purposeInputRef}
                                value={formData.purpose}
                                onChange={(e) => setFormData(
                                    {...formData, purpose: e.target.value})}
                                className="w-full"
                                placeholder="Enter purpose"
                            />
                        </div>
                        {error && (
                            <div className="text-red-500 text-sm">{error}</div>
                        )}
                        <Button
                            type="submit"
                            label="Create Reservation"
                            className="mt-2"
                        />
                    </form>
                </div>
            </div>
        </Card>
    );
};
