/*
 * Copyright (c) 2010 The University of Reading
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
 */

package uk.ac.rdg.resc.edal.coverage.domain;

import java.util.List;
import uk.ac.rdg.resc.edal.coverage.ProfileCoverage;
import uk.ac.rdg.resc.edal.position.VerticalCrs;
import uk.ac.rdg.resc.edal.position.VerticalPosition;

/**
 * The domain of a {@link ProfileCoverage}: a set of vertical positions.
 * @author Jon
 */
public interface ProfileDomain extends DiscreteDomain<VerticalPosition, VerticalPosition>
{
    /**
     * Returns the vertical coordinate reference system used to reference
     * the coordinate values.
     */
    public VerticalCrs getVerticalCrs();

    /**
     * <p>Gets the list of coordinate values, all of which are expressed in the
     * domain's {@link #getVerticalCrs() vertical coordinate reference system}.</p>
     * <p>This may be more convenient to use than {@link #getDomainObjects()}
     * in many cases, but contains the same information, in the same order.</p>
     * @return the list of coordinate values
     */
    public List<Double> getZValues();

}
