/*******************************************************************************
 * Copyright (c) 2014 The University of Reading
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

package uk.ac.rdg.resc.edal.domain;
import java.util.List;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import uk.ac.rdg.resc.edal.geometry.BoundingBox;
import uk.ac.rdg.resc.edal.geometry.LineString;
import uk.ac.rdg.resc.edal.grid.GridCell2D;
import uk.ac.rdg.resc.edal.grid.HorizontalGrid;
import uk.ac.rdg.resc.edal.grid.TimeAxis;
import uk.ac.rdg.resc.edal.position.HorizontalPosition;
import uk.ac.rdg.resc.edal.position.HovmoellerPosition;
import uk.ac.rdg.resc.edal.util.Array1D;

import org.joda.time.DateTime;


/**
 * A measurement of a time series along a line string.
 *
 * @authoer Nan
 */
public class HovmoellerDomain implements DiscreteDomain<HovmoellerPosition, GridCell2D>{
  //central points of the cells that the given line string intersects with the grid
    private Array1D<GridCell2D> domainObjects;
    
    private TimeAxis tAxis;
    private HorizontalGrid hGrid;
    private CoordinateReferenceSystem crs;

    public HovmoellerDomain(LineString lineString, HorizontalGrid hGrid, Extent<DateTime> dateExtent){
        crs =hGrid.getCoordinateReferenceSystem();
        
        //calculate grid cells that the lins string intersects with the grid
        domainObjects =getLineStringIntersectGrid(lineString, hGrid);

        this.hGrid =hGrid;
        tAxis =covertFromDateTimeExtent(dateExtent); 
    }
  
    //need an algorithm to do this task. Guy has done it before.
    private Array1D<HorizontalPosition> getLineStringIntersectGrid(LineString lineString, HorizontalGrid hGrid){
      //...

    }
    
    private TimeAxis covertFromDateTimeExtent(Extent<DateTime> dateExtent){...}

    public boolean contains(HovmoellerPosition p){
    if(p.getTimeExtent().intersects(p.getTimeExtent())){
           for(int i =0; i<domainObjects.size(); i++){
               if(domainObjects.get(i).contains(p.getHorizontalPosition())){
                    return true;
               }
           }
           return false;
        }
        else{
           return false;
        }
    }
    
    public Array1D<GridCell2D> getDomainObjects(){
        return domainObjects;
    }
  
    public CoordinateReferenceSystem getCoordinateReferenceSystem(){
        return crs;
    }

    public TimeAxis getTimeAxis(){
        return tAxis;
    }

    public HorizontalGrid getHorizontalGrid(){
        return hGrid;
    }

    public String toString(){
        return "String";
    }

    public boolean equals(Object obj){
        
    }

    public int hashcode(){
        
    }
    
    public List<HovmoellerPosition> getAllHovmollerPositions(){}

    public List<HorizontalPosition> getHorizontalPositionsOfHovmollerPositions(){}

    public BoundingBox getBoundingBox(){ }
}
