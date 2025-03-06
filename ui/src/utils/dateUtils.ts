/**
 * Converts a Date object to a string in the format YYYY-MM-DD
 * @param date The date to convert
 * @returns A string representation of the date in YYYY-MM-DD format
 */
export const toDateString = (date: Date): string => {
    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();
    return `${year}-${month >= 10 ? month : '0' + month}-${day >= 10 ? day : '0' + day}`;
};

/**
 * Function to create a Date object from a string in 'yyyy-MM-dd' format
 * Hours, minutes, seconds are set to 0 in the local timezone
 *
 * @param dateStr - Date string in 'yyyy-MM-dd' format (e.g. '2025-03-04')
 * @returns Date object with hours, minutes, seconds set to 0
 * @throws Error if the input string is not in the correct format
 */
export const fromDateString = (dateStr: string): Date => {
    // Validate if the string matches 'yyyy-MM-dd' format using regex
    const regex = /^\d{4}-\d{2}-\d{2}$/;
    if (!regex.test(dateStr)) {
        throw new Error("Invalid date format. Expected format: 'yyyy-MM-dd'");
    }

    // Split the date string into year, month, and day
    const [yearStr, monthStr, dayStr] = dateStr.split('-');

    // Convert to numbers
    const year = parseInt(yearStr, 10);
    const month = parseInt(monthStr, 10) - 1; // JavaScript months are 0-11
    const day = parseInt(dayStr, 10);

    // Create a new Date object and set hours, minutes, seconds, milliseconds to 0
    const date = new Date(year, month, day);
    date.setHours(0, 0, 0, 0);

    // Validate if the date is valid
    if (date.getFullYear() !== year || date.getMonth() !== month || date.getDate() !== day) {
        throw new Error("Invalid date. Please check the year, month, and day values.");
    }

    return date;
};

export const convertTimeFormat = (timeString: string): string => {
    const parts = timeString.split(':');

    // If there are 3 or more elements, join the first 2 elements with ':'
    if (parts.length >= 3) {
        return `${parts[0]}:${parts[1]}`;
    }

    // Otherwise, return the input string as is
    return timeString;
};