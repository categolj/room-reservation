import {FC} from 'react';
import styles from './RoomSchedule.module.css';

interface DragSelectionProps {
    isDragging: boolean;
    dragStart: number | null;
    dragEnd: number | null;
    containerRef: React.RefObject<HTMLDivElement>;
    calculateTimeFromY: (y: number) => string;
}

export const DragSelection: FC<DragSelectionProps> = ({
    isDragging,
    dragStart,
    dragEnd,
    containerRef,
    calculateTimeFromY
}) => {
    if (!isDragging || dragStart === null || dragEnd === null || !containerRef.current) {
        return null;
    }

    const rect = containerRef.current.getBoundingClientRect();
    const top = ((Math.min(dragStart, dragEnd) - rect.top) * 100) / rect.height;
    const height = (Math.abs(dragEnd - dragStart) * 100) / rect.height;

    return (
        <div
            className={styles.dragSelection}
            style={{
                top: `${top}%`,
                height: `${height}%`
            }}
        >
            <div className={styles.timeGuide}>
                {calculateTimeFromY(Math.min(dragStart, dragEnd))} - {calculateTimeFromY(
                Math.max(dragStart, dragEnd))}
            </div>
        </div>
    );
};