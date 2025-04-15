import {useRef, useState} from 'react'
import {useNavigate, useParams} from 'react-router-dom'
import {Availability} from '../types'
import {Calendar} from 'primereact/calendar'
import {DataTable} from 'primereact/datatable'
import {Column} from 'primereact/column'
import {Toast} from 'primereact/toast'
import {Card} from 'primereact/card'
import {ProgressSpinner} from 'primereact/progressspinner'
import {Button} from 'primereact/button'
import {fromDateString, toDateString} from '../utils/dateUtils';
import {useAvailabilities} from '../hooks/apiHooks';

export const RoomList = () => {
    const navigate = useNavigate();
    const {date: urlDate} = useParams<{ date: string }>();
    const [date, setDate] = useState<Date>(() => {
        if (urlDate) {
            return fromDateString(urlDate);
        }
        return new Date();
    });
    const toast = useRef<Toast>(null);

    // Use SWR hook for fetching availabilities
    const dateStr = toDateString(date);
    const {data: availabilities, error, isLoading} = useAvailabilities(dateStr);

    // Show error toast if needed
    if (error) {
        toast.current?.show({
            severity: 'error',
            summary: 'Error',
            detail: error.message || 'An error occurred',
            life: 3000
        });
    }

    const handleScheduleClick = (roomId: string) => {
        navigate(`/${dateStr}/rooms/${roomId}`);
    };

    const header = (
        <div className="flex flex-column gap-3">
            <div className="text-xl font-bold">Room Availability</div>
            <div className="flex align-items-center gap-3">
                <Button
                    icon="pi pi-chevron-left"
                    className="p-button-rounded p-button-text"
                    onClick={() => {
                        const prevDate = new Date(date);
                        prevDate.setDate(prevDate.getDate() - 1);
                        const dateStr = toDateString(prevDate);
                        setDate(prevDate);
                        navigate(`/${dateStr}/rooms`);
                    }}
                    tooltip="Previous Day"
                    tooltipOptions={{ position: 'top' }}
                />
                <Calendar
                    id="date"
                    value={date}
                    onChange={(e) => {
                        const newDate = e.value as Date;
                        const dateStr = toDateString(newDate);
                        setDate(newDate);
                        navigate(`/${dateStr}/rooms`);
                    }}
                    showIcon
                    dateFormat="yy-mm-dd"
                    className="p-inputtext-sm"
                />
                <Button
                    icon="pi pi-chevron-right"
                    className="p-button-rounded p-button-text"
                    onClick={() => {
                        const nextDate = new Date(date);
                        nextDate.setDate(nextDate.getDate() + 1);
                        const dateStr = toDateString(nextDate);
                        setDate(nextDate);
                        navigate(`/${dateStr}/rooms`);
                    }}
                    tooltip="Next Day"
                    tooltipOptions={{ position: 'top' }}
                />
            </div>
            <div className="text-lg">
                Available Rooms for {dateStr}
            </div>
        </div>
    );

    return (
        <Card className="mt-4">
            <Toast ref={toast}/>

            <DataTable
                value={availabilities || []}
                header={header}
                emptyMessage="No rooms available for this date"
                loading={isLoading}
                stripedRows
                showGridlines
                responsiveLayout="scroll"
                className="p-datatable-sm"
            >
                <Column
                    field="roomName"
                    header="Room Name"
                    className="font-semibold"
                />
                <Column
                    header="Actions"
                    body={(rowData: Availability) => (
                        <Button
                            icon="pi pi-calendar"
                            className="p-button-rounded p-button-info p-button-sm"
                            onClick={() => handleScheduleClick(rowData.roomId)}
                            tooltip="View Schedule"
                        />
                    )}
                />
            </DataTable>

            {isLoading && (
                <div className="flex justify-content-center mt-4">
                    <ProgressSpinner
                        style={{width: '50px', height: '50px'}}
                        strokeWidth="4"
                        animationDuration=".5s"
                    />
                </div>
            )}
        </Card>
    );
};
