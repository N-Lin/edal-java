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
package uk.ac.rdg.resc.edal.graphics.formats;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.joda.time.DateTime;
import org.opengis.metadata.extent.GeographicBoundingBox;

/**
 * Abstract superclass for simple image formats that do not require information
 * about the layer, time values, bounding box etc to render an image.
 * 
 * @author Jon Blower
 */
public abstract class SimpleFormat extends ImageFormat {

    /**
     * Returns false: simple formats do not require a legend.
     */
    @Override
    public final boolean requiresLegend() {
        return false;
    }

    @Override
    /**
     * Delegates to writeImage(frames, out), ignoring most of the parameters.
     */
    public void writeImage(List<BufferedImage> frames, OutputStream out, String name,
            String description, GeographicBoundingBox bbox, List<DateTime> tValues, String zValue,
            BufferedImage legend, Integer frameRate) throws IOException {
        this.writeImage(frames, out, frameRate);
    }

    /**
     * Writes the given list of {@link java.awt.BufferedImage}s to the given
     * OutputStream. If this ImageFormat doesn't support animations then the
     * given list of frames should only contain one entry, otherwise an
     * IllegalArgumentException will be thrown.
     * 
     * @param frames
     *            List of BufferedImages to render into an image
     * @param out
     *            The OutputStream to which the image will be written
     * @param frameRate
     *            The frame rate to use if this is an animation.
     * @throws IOException
     *             if there was an error writing to the output stream
     * @throws IllegalArgumentException
     *             if this ImageFormat cannot render all of the given
     *             BufferedImages.
     */
    public abstract void writeImage(List<BufferedImage> frames, OutputStream out, Integer frameRate)
            throws IOException;
}
