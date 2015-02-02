/*******************************************************************************
 * Copyright (c) 2013 The University of Reading
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
 ******************************************************************************/

package uk.ac.rdg.resc.edal.util;

import java.util.Iterator;

public abstract class Array1D<T> implements Array<T> {

    private int size;

    public Array1D(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Size must be at least 1");
        }
        this.size = size;
    }

    @Override
    public final int getNDim() {
        return 1;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int counter = 0;

            boolean done = false;

            @Override
            public boolean hasNext() {
                return (!done);
            }

            @Override
            public T next() {
                T value = get(counter);
                /*
                 * Increment the counters if necessary, resetting to zero if
                 * necessary
                 */
                counter++;
                if (counter >= size) {
                    done = true;
                }
                return value;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove is not supported for this iterator");
            }
        };
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public int[] getShape() {
        return new int[] { size };
    }

    @Override
    public int hashCode() {
        int hashValue = 0;
        while (iterator().hasNext()) {
            hashValue += iterator().next().hashCode();
        }
        return hashValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null) {
            return false;
        } else {
            try {
                /*
                 * Cast obj to Arry1D<T>. If the casting fails, of course it
                 * return false. So no need to check if the casting is right or
                 * not.
                 */
                @SuppressWarnings("unchecked")
                Array1D<T> other = (Array1D<T>) obj;
                // Two arrays have different size, they are not equal.
                if (other.size != size) {
                    return false;
                } else {
                    for (int i = 0; i < size; i++) {
                        if (!other.get(i).equals(get(i))) {
                            return false;
                        }
                    }
                    return true;
                }
            } catch (ClassCastException e) {
                return false;
            }
        }
    }
}
