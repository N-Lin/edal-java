/*******************************************************************************
 * Copyright (c) 2011 The University of Reading
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University of Reading, nor the names of the
 *    authors or contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/

package uk.ac.rdg.resc.edal.position;

/**
 * <p>
 * Identifies a point in time. A new type is required here because (1) intrinsic
 * Java types (java.util.Date etc) are inconvenient; and (2) Joda-time types
 * cannot be used in some environments (e.g. Google Web Toolkit).
 * Implementations should be created for different environments, preferably by
 * wrapping existing classes.
 * </p>
 * <p>
 * Defines a natural ordering that is consistent with equals.
 * </p>
 * 
 * @todo Methods could be name-compatible with Joda.
 * @todo Include a precision argument? It will be common for times to be known
 *       only to the level of a certain field (second, day, month, year etc).
 *       This may help with creating sensible user interfaces that don't imply
 *       spurious precision.
 * @author Jon Blower
 */
public interface TimePosition extends Comparable<TimePosition> {

    /**
     * Gets the value of the time coordinate expressed as "milliseconds since
     * 1970-01-01T00:00:00.000Z", where "1970-01-01T00:00:00.000Z" is expressed
     * in the {@link #getCalendarSystem() calendar system}.
     * 
     * @return the value of the time coordinate
     */
    public long getValue();

    /**
     * @return the year represented by this {@link TimePosition}
     */
    public int getYear();

    /**
     * @return the month of the year represented by this {@link TimePosition}
     */
    public int getMonthOfYear();

    /**
     * @return the day of the year represented by this {@link TimePosition}
     */
    public int getDayOfYear();

    /**
     * @return the day of the month represented by this {@link TimePosition}
     */
    public int getDayOfMonth();

    /**
     * @return the hour of the day represented by this {@link TimePosition}
     */
    public int getHourOfDay();

    /**
     * @return the minute part of this {@link TimePosition}
     */
    public int getMinuteOfHour();

    /**
     * @return the second part of this {@link TimePosition}
     */
    public int getSecondOfMinute();

    /**
     * @return the millisecond part of this {@link TimePosition}
     */
    public int getMillisecondOfSecond();

    /**
     * Gets the offset from GMT in <b>minutes</b>.
     */
    public int getTimeZoneOffset();

    /**
     * Returns the calendar system in which the field values and temporal datum
     * are to be interpreted.
     * 
     * @return the calendar system in which the field values and temporal datum
     *         are to be interpreted. Must never be null.
     */
    public CalendarSystem getCalendarSystem();

    /**
     * <p>
     * Compares this time position with another for order along the timeline.
     * Returns a negative integer if this object is before the other object on
     * the timeline, a positive integer if it is after the other object, or zero
     * if they represent the same point.
     * </p>
     * <p>
     * TimePositions are only comparable if they are defined in the same
     * CalendarSystem. In some cases positions may be convertible among
     * calendars, but implementations of this methods must not do this; this
     * conversion must be performed externally.
     * </p>
     * <p>
     * This definition is therefore <b>consistent with equals</b>.
     * </p>
     * 
     * @param t
     *            The time with which this position is to be compared.
     * @return a negative integer if this object is before the other object on
     *         the timeline, a positive integer if it is after the other object,
     *         or zero if they represent the same point.
     * @throws IllegalArgumentException
     *             if {@code t} is not in the same calendar system as this
     *             object.
     */
    @Override
    public int compareTo(TimePosition t);

    /**
     * For two TimePositions to be equal, they must be defined in the same
     * calendar system and have the same {@link #getValue() time coordinate
     * value}. This definition is consistent with the
     * {@link #compareTo(uk.ac.rdg.resc.edal.time.TimePosition)} method.
     * Subclasses should not attempt to perform any conversion between calendar
     * systems in the equals() method.
     */
    @Override
    public boolean equals(Object other);

    /**
     * Adds a period of time to the current {@link TimePosition} and returns a
     * new {@link TimePosition}. This method should not alter the current
     * {@link TimePosition}
     * 
     * @param period
     *            The period of time to add
     * @return A new {@link TimePosition} which is equal to the current
     *         {@link TimePosition} + period
     */
    public TimePosition plus(TimePeriod period);

    /**
     * Subtracts a period of time from the current {@link TimePosition} and
     * returns a new {@link TimePosition}. This method should not alter the
     * current {@link TimePosition}
     * 
     * @param period
     *            The period of time to subtract
     * @return A new {@link TimePosition} which is equal to the current
     *         {@link TimePosition} - period
     */
    public TimePosition minus(TimePeriod period);

    /**
     * Finds the difference, in milliseconds between the current
     * {@link TimePosition} and a supplied {@link TimePosition}.
     * 
     * A positive value implies that the current {@link TimePosition} represents
     * a time later than the supplied time parameter
     * 
     * @param time
     *            The time
     * @return The difference, in milliseconds between the two times
     */
    public long differenceInMillis(TimePosition time);
}