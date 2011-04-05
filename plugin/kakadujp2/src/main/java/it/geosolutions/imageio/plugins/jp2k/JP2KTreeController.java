/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2008, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.plugins.jp2k;

import it.geosolutions.imageio.plugins.jp2k.box.BitsPerComponentBox;
import it.geosolutions.imageio.plugins.jp2k.box.CodestreamHeaderBox;
import it.geosolutions.imageio.plugins.jp2k.box.ColorSpecificationBox;
import it.geosolutions.imageio.plugins.jp2k.box.ComponentMappingBox;
import it.geosolutions.imageio.plugins.jp2k.box.CompositingLayerHeaderBox;
import it.geosolutions.imageio.plugins.jp2k.box.ContiguousCodestreamBox;
import it.geosolutions.imageio.plugins.jp2k.box.FileTypeBox;
import it.geosolutions.imageio.plugins.jp2k.box.IPRBox;
import it.geosolutions.imageio.plugins.jp2k.box.ImageHeaderBox;
import it.geosolutions.imageio.plugins.jp2k.box.JP2HeaderBox;
import it.geosolutions.imageio.plugins.jp2k.box.JP2KFileBox;
import it.geosolutions.imageio.plugins.jp2k.box.PaletteBox;
import it.geosolutions.imageio.plugins.jp2k.box.ReaderRequirementsBox;
import it.geosolutions.imageio.plugins.jp2k.box.ResolutionBox;
import it.geosolutions.imageio.plugins.jp2k.box.SignatureBox;
import it.geosolutions.imageio.plugins.jp2k.box.FileTypeBox.JPEG2000FileType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class JP2KTreeController implements JP2KTreeChecker, TreeModelListener{
    private final static Logger LOGGER = Logger.getLogger("it.geosolutions.imageio.plugins.jp2k");
    
    private static Throwable checkCauses(final Collection<Throwable> causes){
            if (causes == null || causes.size() == 0)
                    throw new IllegalArgumentException("Causes cannot be null or empty for this MultiCauseIllegalStateException");
            return causes.iterator().next();
    }
    
    /**
     * Simple subclass of {@link IllegalStateException} which can be used to report multiple causes.
     * 
     * @author Simone Giannecchini, GeoSolutions
     *
     */
    public final class MultiCauseIllegalStateException extends IllegalStateException{

            /**
             * 
             */
            private static final long serialVersionUID = 8965184317402766015L;
            
            private ArrayList<Throwable> causes;

            public MultiCauseIllegalStateException(final String message,final Collection<Throwable>  causes){
                    super(message,checkCauses(causes));
                    this.causes= new ArrayList<Throwable>(causes);
            }
            
            public MultiCauseIllegalStateException(final Collection<Throwable>  causes){
                    super(checkCauses(causes));
                    this.causes= new ArrayList<Throwable>(causes);
            }

            public List<Throwable> getCauses() {
                    return Collections.unmodifiableList(causes);
            }
    }
    
    abstract class PolicyCheck{
            List<? extends Throwable> checkNodesInserted(
                            final  JP2KBox node,
                            final int[] childrenIndices,
                            final Object[] children){
                    return Collections.emptyList();
            }
            
            List<? extends Throwable> checkTreeConsistency(){
            	 return Collections.emptyList();
            }
    }
    
    class UnspecifiedPolicy extends PolicyCheck{

            @Override
            List<? extends Throwable> checkNodesInserted(
                            final  JP2KBox node,
                            final int[] childrenIndices,
                            final Object[] children) {
                    
                    switch(node.getType()){
                    case JP2KFileBox.BOX_TYPE:
                            if (childrenIndices.length >= 1 && children != null) {
                                    for (int i = 0; i < childrenIndices.length; i++) {
                                            final int index = childrenIndices[i];
                                            final Object child = children[i];
                                            if(child==null)
                                                continue;
                                            switch(index){
                                            case 0:
                                                    final JP2KBox signature = (JP2KBox) child;
                                                    if (signature.getType() != SignatureBox.BOX_TYPE)
                                                        return Arrays.asList(new IllegalStateException("First box of a JPEG2000 file must be the SignatureBox"));
                                                    final byte[] content = signature.getContent();
                                                    final byte[] expectedContent = SignatureBox.LOCAL_DATA;
                                                    if(expectedContent.length!=content.length)
                                                        return Arrays.asList(new IllegalStateException("SignatureBox content incorrect"));  
                                                    for(int j=0;j<expectedContent.length;j++)
                                                            if(expectedContent[j]!=content[j])
                                                                    return Arrays.asList(new IllegalStateException("SignatureBox content incorrect"));
                                                    return super.checkNodesInserted(node, childrenIndices, children);
                                            case 1:
                                                    JP2KBox box = (JP2KBox) child;
                                                    if (box.getType() != FileTypeBox.BOX_TYPE)
                                                        return Arrays.asList(new IllegalStateException("Second box of a JPEG2000 file must be the FileTypeBox"));
                                                    //let's convert it
                                                    if(box instanceof LazyJP2KBox)
                                                            box = ((LazyJP2KBox) box).getOriginalBox();
                                                    JP2KTreeController.this.fileType=((FileTypeBox)box).getBrand();
                                                    JP2KTreeController.this.compatibilitySet=((FileTypeBox)box).getCompatibilitySet();
                                                    if(!compatibilitySet.contains(JPEG2000FileType.JP2))
                                                            return Arrays.asList(new IllegalStateException("We are only able to serve JP2 compatible files"));
                                                    return super.checkNodesInserted(node, childrenIndices, children);
                                            }
                                            
                                    }
                            }
                    }
                    return Arrays.asList(new IllegalStateException("This node should not be inserted while the file type is still unspecified"));
            }
    }
    
    class JP2Policy extends PolicyCheck{
            @Override
            List<? extends Throwable> checkNodesInserted(
                            final  JP2KBox node,
                            final int[] childrenIndices,
                            final Object[] children) {
                    List<IllegalStateException> errors = new ArrayList<IllegalStateException>();
                
                    final Object root = model.getRoot();
                    final JP2KFileBox fileBox;
                    if (root!=null && root instanceof JP2KFileBox){
                       fileBox = (JP2KFileBox) root;
                    }
                    else 
                       throw new IllegalStateException("Root node unavailable");
                
                    switch(node.getType()){
                    
                    // ////////////////////////////////////////////////////
                    //
                    // Checking the FileBox
                    //
                    // ////////////////////////////////////////////////////
                    case JP2KFileBox.BOX_TYPE:
                        if(childrenIndices.length>=1&&children!=null){
                            for(int i=0;i<childrenIndices.length;i++){
                                final int index = childrenIndices[i];
                                final Object child = children[i];
                                if(child==null)
                                    continue;
                                final JP2KBox candidate = (JP2KBox) child;
                                final int childType = candidate.getType();
                                switch (childType){
                                
                                // //
                                //
                                // Checking the JP2HeaderBox
                                // 
                                // //  
                                case JP2HeaderBox.BOX_TYPE:
                                    final int contiguousCodestreamBoxIndex = getChildIndex(node, ContiguousCodestreamBox.BOX_TYPE);
                                    if (contiguousCodestreamBoxIndex!=-1)
                                        if (contiguousCodestreamBoxIndex<=index)
                                            errors.add(new IllegalStateException("ContiguousCodestream Box must appear after the JP2Header Box"));
                                    break;
                                    
                                // //
                                //
                                // Checking the ContiguousCodestreamBox
                                // 
                                // // 
                                case ContiguousCodestreamBox.BOX_TYPE:
                                    final int jp2HeaderBoxIndex = getChildIndex(node, JP2HeaderBox.BOX_TYPE);
                                    if (jp2HeaderBoxIndex!=-1)
                                        if (jp2HeaderBoxIndex>=index)
                                            errors.add(new IllegalStateException("JP2Header Box must appear before the ContiguousCodestream"));
                                    break;
                                    
                                // //
                                //
                                // Checking the IPRBox
                                // 
                                // //    
                                case IPRBox.BOX_TYPE:
                                    final int jp2HeaderIndex = getChildIndex(node, JP2HeaderBox.BOX_TYPE);
                                    if (jp2HeaderIndex!=-1){
                                        final JP2KBox jp2HeaderBox = (JP2KBox) node.getChildAt(jp2HeaderIndex);
                                        final int imageHeaderBoxIndex = getChildIndex(jp2HeaderBox, ImageHeaderBox.BOX_TYPE);
                                        if (imageHeaderBoxIndex!=-1){
                                            final JP2KBox ihBox = (JP2KBox) jp2HeaderBox.getChildAt(imageHeaderBoxIndex);
                                            final ImageHeaderBox imageHeaderBox = (ImageHeaderBox)LazyJP2KBox.getAsOriginalBox(ihBox);
                                            if (imageHeaderBox.getIntellectualProperty()!=1){
                                                errors.add(new IllegalStateException("IPRBox needs ImageHeaderBox's IP flag set to 1"));
                                            }
                                        }
                                    }
                                    break;
                                } 
                            }
                        }
                        
                        
                    // ////////////////////////////////////////////////////
                    //
                    // Checking the JP2Header Box
                    //
                    // ////////////////////////////////////////////////////
                    case JP2HeaderBox.BOX_TYPE:
                        if(childrenIndices.length>=1 && children!=null){
                            for(int i=0;i<childrenIndices.length;i++){
                                final int index = childrenIndices[i];
                                final Object child = children[i];
                                if (child == null)
                                    continue;
                                final JP2KBox childBox = (JP2KBox) child;
                                final int boxType = childBox.getType(); 
                                if (index == 0) {
                                    // //
                                    //
                                    // Checking the ImageHeader Box
                                    // ----------------------------
                                    // ImageHeaderBox has some bytes which allow to know additional information
                                    // about additional Boxes. As an instance, in case the IP parameter is set
                                    // to 1, then an IPR box should be present. 
                                    // Then, I need to check ImageHeaderBox content is consistent with the
                                    // already inserted nodes. 
                                    // 
                                    // //
                                    if (boxType != ImageHeaderBox.BOX_TYPE)
                                        throw new IllegalStateException(
                                                "First box of a JP2 Header Box must be the ImageHeaderBox");
                                    final ImageHeaderBox imageHeaderBox = (ImageHeaderBox)LazyJP2KBox.getAsOriginalBox(childBox);
                                    // //
                                    //
                                    // Checking IntellectualProperty.
                                    // If IP is 0, then the JP2 File shall not contain a
                                    // IntellectualPropertyBox
                                    //
                                    // //
                                    final byte ip = imageHeaderBox.getIntellectualProperty();
                                    if (ip == 0) {
                                        final int childBoxIndex = getChildIndex(fileBox, IPRBox.BOX_TYPE);
                                        if (childBoxIndex!=-1)
                                            errors.add(new IllegalStateException("IntellectualProperty Box shall " +
                                                            "not be defined when ImageHeaderBox has IP=0"));
                                    }
                                    
                                    // //
                                    //
                                    // Checking BitDepth is consistent with the BitsPerComponent Box.
                                    //
                                    // //
                                    final byte bitDepth = imageHeaderBox.getBitDepth();
                                    if (bitDepth != 0xFF){
                                        // Specification states that in case all components have the same
                                        // bit-depth, then the BitDepth contains that value and a 
                                        // BitsPerComponent box shall not exist. Otherwise, 
                                        // the BitDepth parameter should be 0xFF
                                        final int childBoxIndex = getChildIndex(fileBox, BitsPerComponentBox.BOX_TYPE);
                                        if (childBoxIndex!=-1)
                                            errors.add(new IllegalStateException("BitsPerComponent Box shall not be defined when ImageHeaderBox has BitDepth != 0xFF"));
                                    }
                                }else{
                                    // //
                                    //
                                    // Checking the BitsPerComponent Box
                                    // 
                                    // //
                                    if (boxType == BitsPerComponentBox.BOX_TYPE){
                                        final int imageHeaderBoxIndex = getChildIndex(node, ImageHeaderBox.BOX_TYPE);
                                        if (imageHeaderBoxIndex!=-1){
                                            final JP2KBox ihBox = (JP2KBox) node.getChildAt(imageHeaderBoxIndex);
                                            final ImageHeaderBox imageHeaderBox = (ImageHeaderBox)LazyJP2KBox.getAsOriginalBox(ihBox);
                                            
                                            if (imageHeaderBox.getBitDepth()!=0xFF){
                                                errors.add(new IllegalStateException("BitsPerComponent Box shall not be defined when ImageHeaderBox has BitDepth != 0xFF"));
                                            }
                                        }
                                    }
                                    // //
                                    //
                                    // Checking the ColorSpecification Box
                                    // 
                                    // //
                                    else if (boxType == ColorSpecificationBox.BOX_TYPE){
                                        final ColorSpecificationBox csBox = (ColorSpecificationBox)LazyJP2KBox.getAsOriginalBox(childBox);
                                        final byte method = csBox.getMethod();
                                        if (method==1){
                                            if (csBox.getICCProfile()!=null){
                                                //TODO: maybe avoid adding an error -> Simply logging a warning
                                                errors.add(new IllegalStateException("ColorSpecification Box with method = 1 should have a NULL ICP"));
                                                
                                            }
                                            final int ecs = csBox.getEnumeratedColorSpace();
                                            if (ecs!=ColorSpecificationBox.ECS_GRAY && ecs!=ColorSpecificationBox.ECS_sRGB &&
                                                    ecs!=ColorSpecificationBox.ECS_YCC)
                                                errors.add(new IllegalStateException("Unsupported Enumerated Color Space in ColorSpecification Box"));
                                        }
                                        else if (method==2){
                                            if (csBox.getEnumeratedColorSpace()!=-1)
                                                errors.add(new IllegalStateException("ColorSpecification Box with method = 2 shouldn't have an Enumerated Color Space defined"));
                                        }
                                        else
                                            errors.add(new IllegalStateException("ColorSpecification Box only supports value 1 and 2 for the M parameter"));
                                   }
                                    // //
                                    //
                                    // Checking the ComponentMapping Box
                                    // 
                                    // //
                                    else if (boxType == ComponentMappingBox.BOX_TYPE){
//                                        final ComponentMappingBox cmBox = (ComponentMappingBox) child;
                                        //TODO: Add checks
                                    }
                                    
                                    // //
                                    //
                                    // Checking the Resolution Box
                                    // 
                                    // //
                                    else if (boxType == ResolutionBox.BOX_TYPE){
//                                        final ResolutionBox resBox = (ResolutionBox) child;
                                        //TODO: Add checks
                                    }
                                }
                            }
                        }
                        break;
                        
                    case ResolutionBox.BOX_TYPE:
                        
                    }
                    if (errors.size()==0)
                        return super.checkNodesInserted(node, childrenIndices, children);
                    else 
                        return errors;
            }
            
            List<? extends Throwable> checkTreeConsistency() {
                List <IllegalStateException> errors = new ArrayList<IllegalStateException>();
                final Object root = model.getRoot();
                if (root!=null && root instanceof JP2KFileBox){
                   boolean signatureBoxOk = false;
                   boolean fileTypeBoxOk  = false;
                   boolean jp2HeaderBoxIsPresent = false;
                   boolean contiguousCodestreamBoxIsPresent = false;
                   boolean iprBoxIsPresent = false;
                   boolean iprBoxIsRequired = false;
                   
                   // /////////////////////////////////////////////////////////////////
                   //
                   // Checking File Boxes
                   //
                   // /////////////////////////////////////////////////////////////////
                   final JP2KFileBox fileBox = (JP2KFileBox) root;
                   final int rootChildrenCount = fileBox.getChildCount();
                   int jp2HeaderBoxIndex = -1;
                   for (int i=0; i<rootChildrenCount; i++){
                       final JP2KBox child = (JP2KBox) fileBox.getChildAt(i);
                       final int boxType = child.getType();
                       if (boxType==SignatureBox.BOX_TYPE)
                           signatureBoxOk = true;
                       else if (boxType == FileTypeBox.BOX_TYPE)
                           fileTypeBoxOk = true;
                       else if (boxType == IPRBox.BOX_TYPE)
                           iprBoxIsPresent = true;
                       else if (boxType == JP2HeaderBox.BOX_TYPE){
                           jp2HeaderBoxIsPresent = true;
                           jp2HeaderBoxIndex = i;
                       }
                       else if (boxType == ContiguousCodestreamBox.BOX_TYPE)
                           contiguousCodestreamBoxIsPresent = true;
                       
                   }
                   
                   // //
                   //
                   // Checking all required boxes have been found on the file.
                   //
                   // //
                   if (!signatureBoxOk)
                       errors.add(new IllegalStateException("Missing SignatureBox"));
                   if (!fileTypeBoxOk)
                       errors.add(new IllegalStateException("Missing FileTypeBox"));
                   if (!jp2HeaderBoxIsPresent)
                       errors.add(new IllegalStateException("Missing Jp2HeaderBox"));
                   else{
                       // /////////////////////////////////////////////////////////
                       //
                       // Checking JP2Header Boxes
                       //
                       // /////////////////////////////////////////////////////////
                       JP2KBox jp2header = (JP2KBox) fileBox.getChildAt(jp2HeaderBoxIndex);
                       final int jp2HeaderChildrenCount = jp2header.getChildCount();
                       
                       // //
                       // Init booleans for checks
                       // //
                       boolean imageHeaderBoxOk = false;
                       boolean bitsPerComponentBoxIsPresent = false;
                       boolean colorSpecificationBoxOk = false;
                       boolean paletteBoxIsPresent = false;
                       boolean componentMappingBoxIsPresent = false;
                       boolean resolutionBoxIsPresent = false;
                       boolean resolutionBoxIsOk = false;
                       byte b = 0;
                       
                       // //
                       //
                       // Loop over the JP2HeaderBox children
                       //
                       // //
                       for (int k=0; k<jp2HeaderChildrenCount; k++){
                           final JP2KBox jp2HeaderChild = (JP2KBox) jp2header.getChildAt(k);
                           final int childboxType = jp2HeaderChild.getType();
                           
                           // //
                           // Check ImageHeaderBox consistency
                           // //
                           if (childboxType==ImageHeaderBox.BOX_TYPE){
                               imageHeaderBoxOk = true;
                               final ImageHeaderBox imageHeaderBox = (ImageHeaderBox)LazyJP2KBox.getAsOriginalBox(jp2HeaderChild);
                               b = imageHeaderBox.getBitDepth();
                               iprBoxIsRequired = imageHeaderBox.getIntellectualProperty()==1;
                           } else if (childboxType == BitsPerComponentBox.BOX_TYPE){
                               bitsPerComponentBoxIsPresent = true;
                           } else if (childboxType == ColorSpecificationBox.BOX_TYPE){
                               colorSpecificationBoxOk = true;
                           } else if (childboxType == PaletteBox.BOX_TYPE){
                               paletteBoxIsPresent = true;
                           } else if (childboxType == ComponentMappingBox.BOX_TYPE){
                               componentMappingBoxIsPresent = true;
                           } else if (childboxType == ResolutionBox.BOX_TYPE){
                               resolutionBoxIsPresent = true;
                               resolutionBoxIsOk = (jp2HeaderChild.getChildCount()>0);
                           }
                       }
                       
                       // //
                       //
                       // Check all required boxes have been found on JP2HeaderBox.
                       //
                       // //
                       if (!imageHeaderBoxOk)
                           errors.add(new IllegalStateException("Missing ImageHeaderBox"));
                       if (b!=0xFF && bitsPerComponentBoxIsPresent)
                           errors.add(new IllegalStateException("BitsPerComponentBox shall not be defined when Bit-Depth in ImageHeaderBox is not 0xFF"));
                       if (!colorSpecificationBoxOk)    
                           errors.add(new IllegalStateException("Missing ColorSpecificationBox"));
                       if (resolutionBoxIsPresent && !resolutionBoxIsOk)
                           errors.add(new IllegalStateException("ResolutionBox superbox doesn't contain any child"));
                       if (paletteBoxIsPresent ^ componentMappingBoxIsPresent)
                           errors.add(new IllegalStateException("PaletteBox requires a ComponentMappingBox and viceversa"));
                   }
                   
                   
                   // //
                   // Checking Intellectual Property Rights consistency
                   // //
                   if (iprBoxIsRequired ^ iprBoxIsPresent){
                       errors.add(new IllegalStateException("IPRBox is inconsistent with the ImageHeaderBox IP parameter"));
                   }
                   
                   if (!contiguousCodestreamBoxIsPresent)
                       errors.add(new IllegalStateException("ContiguousCodeStreamBox is missing"));
                }
                if (errors.isEmpty())
                    return Collections.emptyList();
                return errors;
            }
    }
    

    class JPXPolicy extends PolicyCheck{
    	
	        /**
                 * TODO:
                 * 
                 * @todo Rules for checking the JPX consistency are more complex
                 *       with respect to the JP2's ones.
                 */
        @Override
            List<? extends Throwable> checkTreeConsistency() {
                List <IllegalStateException> errors = new ArrayList<IllegalStateException>();
                final Object root = model.getRoot();
                if (root!=null && root instanceof JP2KFileBox){
                   boolean signatureBoxOk = false;
                   boolean fileTypeBoxOk  = false;
                   boolean jp2HeaderBoxIsPresent = false;
                   boolean readerRequirementsBoxIsPresent = false;
                   boolean iprBoxIsPresent = false;
                   boolean iprBoxIsRequired = false;
                   boolean imageHeaderBoxIsPresent = false;
                   int codestreamHeaderBoxes = 0;
                   int numCodestreams = 0;
                   int compositingLayerHeaderBoxes = 0;
                   
                   // /////////////////////////////////////////////////////////////////
                   //
                   // Checking File Boxes
                   //
                   // /////////////////////////////////////////////////////////////////
                   final JP2KFileBox fileBox = (JP2KFileBox) root;
                   final int rootChildrenCount = fileBox.getChildCount();
                   int jp2HeaderBoxIndex = -1;
                   int firstCompositingLayerHeaderBoxIndex = -1;
                   int firstCodestreamHeaderBoxIndex = -1;
                   int firstCodestreamBoxIndex = -1;
                   for (int i=0; i<rootChildrenCount; i++){
                       final JP2KBox child = (JP2KBox) fileBox.getChildAt(i);
                       final int boxType = child.getType();
                       if (boxType==SignatureBox.BOX_TYPE)
                           signatureBoxOk = true;
                       else if (boxType == FileTypeBox.BOX_TYPE)
                           fileTypeBoxOk = true;
                       else if (boxType == IPRBox.BOX_TYPE)
                           iprBoxIsPresent = true;
                       else if (boxType == ReaderRequirementsBox.BOX_TYPE)
                           readerRequirementsBoxIsPresent = true;
                       else if (boxType == JP2HeaderBox.BOX_TYPE){
                           jp2HeaderBoxIsPresent = true;
                           jp2HeaderBoxIndex = i;
                       }
                       else if (boxType == CompositingLayerHeaderBox.BOX_TYPE){
                    	   if (firstCompositingLayerHeaderBoxIndex ==-1)
                    		   firstCompositingLayerHeaderBoxIndex = i;
                    	   compositingLayerHeaderBoxes++;
                       }
                       else if (boxType == CodestreamHeaderBox.BOX_TYPE){
                    	   if (firstCodestreamHeaderBoxIndex == -1)
                    		   firstCodestreamHeaderBoxIndex = i;
                    	   codestreamHeaderBoxes++;
                       }
                       else if (boxType == ContiguousCodestreamBox.BOX_TYPE){
                    	   if (firstCodestreamBoxIndex == -1)
                    		   firstCodestreamBoxIndex = i;
                    	   numCodestreams++;
                       }
                   }
                   
                   // //
                   //
                   // Checking all required boxes have been found on the file.
                   //
                   // //
                   if (!signatureBoxOk)
                       errors.add(new IllegalStateException("Missing SignatureBox"));
                   if (!readerRequirementsBoxIsPresent)
                       errors.add(new IllegalStateException("Missing ReaderRequirementsBox"));
                   if (!fileTypeBoxOk)
                       errors.add(new IllegalStateException("Missing FileTypeBox"));
                   if (!jp2HeaderBoxIsPresent){
                	   if (firstCodestreamHeaderBoxIndex!=-1){
                		   final JP2KBox codestreamHeaderBox = (JP2KBox) fileBox.getChildAt(firstCodestreamHeaderBoxIndex);
                		   final int codestreamChildrenCount = fileBox.getChildCount();
                           for (int i=0; i<codestreamChildrenCount; i++){
                               final JP2KBox child = (JP2KBox) codestreamHeaderBox.getChildAt(i);
                               final int boxType = child.getType();
                               if (boxType==ImageHeaderBox.BOX_TYPE){
                            	   imageHeaderBoxIsPresent = true;
                            	   break;
                               }
                           }
                	   }
                	   else
                		   errors.add(new IllegalStateException("Missing both JP2HeaderBox and CodeStreamHeaderBox"));
                	   if (firstCompositingLayerHeaderBoxIndex!=-1){
                		   //TODO:Check for the colourSpecificationBox
                		   
                	   }
                   } else{
                       // /////////////////////////////////////////////////////////
                       //
                       // Checking JP2Header Boxes
                       //
                       // /////////////////////////////////////////////////////////
                       JP2KBox jp2header = (JP2KBox) fileBox.getChildAt(jp2HeaderBoxIndex);
                       final int jp2HeaderChildrenCount = jp2header.getChildCount();
                       
                       // //
                       // Init booleans for checks
                       // //
                       boolean bitsPerComponentBoxIsPresent = false;
                       boolean colourSpecificationBoxOk = false;
                       boolean paletteBoxIsPresent = false;
                       boolean componentMappingBoxIsPresent = false;
                       boolean resolutionBoxIsPresent = false;
                       boolean resolutionBoxIsOk = false;
                       byte b = 0;
                       
                       // //
                       //
                       // Loop over the JP2HeaderBox children
                       //
                       // //
                       for (int k=0; k<jp2HeaderChildrenCount; k++){
                           final JP2KBox jp2HeaderChild = (JP2KBox) jp2header.getChildAt(k);
                           final int childboxType = jp2HeaderChild.getType();
                           
                           // //
                           // Check ImageHeaderBox consistency
                           // //
                           if (childboxType==ImageHeaderBox.BOX_TYPE){
                               imageHeaderBoxIsPresent = true;
                               final ImageHeaderBox imageHeaderBox = (ImageHeaderBox)LazyJP2KBox.getAsOriginalBox(jp2HeaderChild);
                               b = imageHeaderBox.getBitDepth();
                               iprBoxIsRequired = imageHeaderBox.getIntellectualProperty()==1;
                           } else if (childboxType == BitsPerComponentBox.BOX_TYPE){
                               bitsPerComponentBoxIsPresent = true;
                           } else if (childboxType == ColorSpecificationBox.BOX_TYPE){
                               colourSpecificationBoxOk = true;
                           } else if (childboxType == PaletteBox.BOX_TYPE){
                               paletteBoxIsPresent = true;
                           } else if (childboxType == ComponentMappingBox.BOX_TYPE){
                               componentMappingBoxIsPresent = true;
                           } else if (childboxType == ResolutionBox.BOX_TYPE){
                               resolutionBoxIsPresent = true;
                               resolutionBoxIsOk = (jp2HeaderChild.getChildCount()>0);
                           }
                       }
                       //TODO: FIX ME! Check Boxes within JP2HeaderBox are not defined in the first 
                       // CodestreamHeaderBox and CompositingLayerHeaderBox
                       
                       // //
                       //
                       // Check all required boxes have been found on JP2HeaderBox.
                       //
                       // //
                       if (b!=0xFF && bitsPerComponentBoxIsPresent)
                           errors.add(new IllegalStateException("BitsPerComponentBox shall not be defined when Bit-Depth in ImageHeaderBox is not 0xFF"));
                       if (!colourSpecificationBoxOk)    
                           errors.add(new IllegalStateException("Missing ColorSpecificationBox"));
                       if (resolutionBoxIsPresent && !resolutionBoxIsOk)
                           errors.add(new IllegalStateException("ResolutionBox superbox doesn't contain any child"));
                       if (paletteBoxIsPresent ^ componentMappingBoxIsPresent)
                           errors.add(new IllegalStateException("PaletteBox requires a ComponentMappingBox and viceversa"));
                   }
                   
                   if (jp2HeaderBoxIsPresent){
                	   // //
                	   //
                	   // Rule of M.9.2.7
                	   // 
                	   // //
                	   if (firstCodestreamHeaderBoxIndex!=-1 && jp2HeaderBoxIndex>=firstCodestreamHeaderBoxIndex)
                		   errors.add(new IllegalStateException("CodestreamHeader Box must appear after the JP2Header Box"));
                	   if (firstCompositingLayerHeaderBoxIndex!=-1 && jp2HeaderBoxIndex>=firstCompositingLayerHeaderBoxIndex)
                		   errors.add(new IllegalStateException("CompositingLayerHeader Box must appear after the JP2Header Box"));
                	   if (firstCodestreamBoxIndex!=-1 && jp2HeaderBoxIndex>=firstCodestreamBoxIndex)
                		   errors.add(new IllegalStateException("Codestream Box must appear after the JP2Header Box"));
                   }
                   
                   if (!imageHeaderBoxIsPresent)
                       errors.add(new IllegalStateException("Missing ImageHeaderBox"));
                   // //
                   // Checking Intellectual Property Rights consistency
                   // //
                   if (iprBoxIsRequired ^ iprBoxIsPresent){
                       errors.add(new IllegalStateException("IPRBox is inconsistent with the ImageHeaderBox IP parameter"));
                   }
                   
                   if (codestreamHeaderBoxes!=0 && codestreamHeaderBoxes!=numCodestreams){
                	   //TODO: FIX ME! NEED TO TAKE CARE OF FRAGMENT TABLE BOX TOO.
                	   errors.add(new IllegalStateException("The number of codestreams and codestreamHeaderBox shall match"));
                   }
                }
                if (errors.isEmpty())
                    return Collections.emptyList();
                return errors;
            }
            
            /**
			 * TODO:
			 * 
			 * @todo Actual check is a simple copy of the JP2 policy followed by
			 *       a compatibility list check. Rules for checking the JPX
			 *       consistency are more complex with respect to the JP2's
			 *       ones.
			 */
            List<? extends Throwable> checkNodesInserted(
                    final  JP2KBox node,
                    final int[] childrenIndices,
                    final Object[] children) {
            List<IllegalStateException> errors = new ArrayList<IllegalStateException>();
        
            final Object root = model.getRoot();
            final JP2KFileBox fileBox;
            if (root!=null && root instanceof JP2KFileBox){
               fileBox = (JP2KFileBox) root;
            }
            else 
               throw new IllegalStateException("Root node unavailable");
        
            switch(node.getType()){
            
            // ////////////////////////////////////////////////////
            //
            // Checking the FileBox
            //
            // ////////////////////////////////////////////////////
            case JP2KFileBox.BOX_TYPE:
                if(childrenIndices.length>=1&&children!=null){
                    for(int i=0;i<childrenIndices.length;i++){
                        final int index = childrenIndices[i];
                        final Object child = children[i];
                        if(child==null)
                            continue;
                        final JP2KBox candidate = (JP2KBox) child;
                        final int childType = candidate.getType();
                        switch (childType){
                        
                        // //
                        //
                        // Checking the ReaderRequirementsBox
                        // 
                        // //  
                        case ReaderRequirementsBox.BOX_TYPE:
                            final int fileTypeBoxIndex = getChildIndex(node, FileTypeBox.BOX_TYPE);
                            if (fileTypeBoxIndex!=-1)
                                if (fileTypeBoxIndex>=index)
                                    errors.add(new IllegalStateException("ReaderRequirements Box must appear after the FileType Box"));
                            ReaderRequirementsBox rreqbox = (ReaderRequirementsBox)LazyJP2KBox.getAsOriginalBox(candidate);
                            
                            //TODO: Check the GML-JP2 signal: standard Feature Value = 67.
                            break;
                        
                        case JP2HeaderBox.BOX_TYPE:
                            final int contiguousCodestreamBoxIndex = getChildIndex(node, ContiguousCodestreamBox.BOX_TYPE);
                            if (contiguousCodestreamBoxIndex!=-1)
                                if (contiguousCodestreamBoxIndex<=index)
                                    errors.add(new IllegalStateException("ContiguousCodestream Box must appear after the JP2Header Box"));
                            final int codestreamHeaderBoxIndex = getChildIndex(node, CodestreamHeaderBox.BOX_TYPE);
                            if (codestreamHeaderBoxIndex!=-1)
                                if (codestreamHeaderBoxIndex<=index)
                                    errors.add(new IllegalStateException("CodestreamHeader Box must appear after the JP2Header Box"));
                            final int compositingLayerHeaderBox = getChildIndex(node, CompositingLayerHeaderBox.BOX_TYPE);
                            if (compositingLayerHeaderBox!=-1)
                                if (compositingLayerHeaderBox<=index)
                                    errors.add(new IllegalStateException("CompositingLayerHeader Box must appear after the JP2Header Box"));
                            //TODO: Add more checks
                            break;
                            
                        case CompositingLayerHeaderBox.BOX_TYPE:
                            final int jp2HBoxIndex = getChildIndex(node, JP2HeaderBox.BOX_TYPE);
                            if (jp2HBoxIndex!=-1)
                                if (jp2HBoxIndex>=index)
                                    errors.add(new IllegalStateException("CompositingLayerHeader Box must appear after the JP2Header Box"));
                            break;
                        
                        case CodestreamHeaderBox.BOX_TYPE:
                            final int jp2HBoxIndex1 = getChildIndex(node, JP2HeaderBox.BOX_TYPE);
                            if (jp2HBoxIndex1!=-1)
                                if (jp2HBoxIndex1>=index)
                                    errors.add(new IllegalStateException("CodestreamHeader Box must appear after the JP2Header Box"));
                            break;
                            
                        // //
                        //
                        // Checking the ContiguousCodestreamBox
                        // 
                        // // 
                        case ContiguousCodestreamBox.BOX_TYPE:
                            final int jp2HBoxIndex2 = getChildIndex(node, JP2HeaderBox.BOX_TYPE);
                            if (jp2HBoxIndex2!=-1)
                                if (jp2HBoxIndex2>=index)
                                    errors.add(new IllegalStateException("JP2Header Box must appear before the ContiguousCodestream"));
                            break;
                            
                            
                        // //
                        //
                        // Checking the IPRBox
                        // 
                        // //    
                        case IPRBox.BOX_TYPE:
                            final int jp2HeaderIndex = getChildIndex(node, JP2HeaderBox.BOX_TYPE);
                            if (jp2HeaderIndex!=-1){
                                final JP2KBox jp2HeaderBox = (JP2KBox) node.getChildAt(jp2HeaderIndex);
                                final int imageHeaderBoxIndex = getChildIndex(jp2HeaderBox, ImageHeaderBox.BOX_TYPE);
                                if (imageHeaderBoxIndex!=-1){
                                    final JP2KBox ihBox = (JP2KBox) jp2HeaderBox.getChildAt(imageHeaderBoxIndex);
                                    final ImageHeaderBox imageHeaderBox = (ImageHeaderBox)LazyJP2KBox.getAsOriginalBox(ihBox);
                                    if (imageHeaderBox.getIntellectualProperty()!=1){
                                        errors.add(new IllegalStateException("IPRBox needs ImageHeaderBox's IP flag set to 1"));
                                    }
                                }
                            }
                            break;
                        } 
                    }
                }
                
                
            // ////////////////////////////////////////////////////
            //
            // Checking the JP2Header Box
            //
            // ////////////////////////////////////////////////////
            case JP2HeaderBox.BOX_TYPE:
                if(childrenIndices.length>=1 && children!=null){
                    for(int i=0;i<childrenIndices.length;i++){
                        final int index = childrenIndices[i];
                        final Object child = children[i];
                        if (child == null)
                            continue;
                        final JP2KBox childBox = (JP2KBox) child;
                        final int boxType = childBox.getType(); 
                        if (index == 0) {
                            // //
                            //
                            // Checking the ImageHeader Box
                            // ----------------------------
                            // ImageHeaderBox has some bytes which allow to know additional information
                            // about additional Boxes. As an instance, in case the IP parameter is set
                            // to 1, then an IPR box should be present. 
                            // Then, I need to check ImageHeaderBox content is consistent with the
                            // already inserted nodes. 
                            // 
                            // //
                            if (boxType != ImageHeaderBox.BOX_TYPE)
                                throw new IllegalStateException(
                                        "First box of a JP2 Header Box must be the ImageHeaderBox");
                            final ImageHeaderBox imageHeaderBox = (ImageHeaderBox)LazyJP2KBox.getAsOriginalBox(childBox);
                           
                            // //
                            //
                            // Checking IntellectualProperty.
                            // If IP is 0, then the JP2 File shall not contain a
                            // IntellectualPropertyBox
                            //
                            // //
                            final byte ip = imageHeaderBox.getIntellectualProperty();
                            if (ip == 0) {
                                final int childBoxIndex = getChildIndex(fileBox, IPRBox.BOX_TYPE);
                                if (childBoxIndex!=-1)
                                    errors.add(new IllegalStateException("IntellectualProperty Box shall " +
                                                    "not be defined when ImageHeaderBox has IP=0"));
                            }
                            
                            // //
                            //
                            // Checking BitDepth is consistent with the BitsPerComponent Box.
                            //
                            // //
                            final byte bitDepth = imageHeaderBox.getBitDepth();
                            if (bitDepth != 0xFF){
                                // Specification states that in case all components have the same
                                // bit-depth, then the BitDepth contains that value and a 
                                // BitsPerComponent box shall not exist. Otherwise, 
                                // the BitDepth parameter should be 0xFF
                                final int childBoxIndex = getChildIndex(fileBox, BitsPerComponentBox.BOX_TYPE);
                                if (childBoxIndex!=-1)
                                    errors.add(new IllegalStateException("BitsPerComponent Box shall not be defined when ImageHeaderBox has BitDepth != 0xFF"));
                            }
                        }else{
                            // //
                            //
                            // Checking the BitsPerComponent Box
                            // 
                            // //
                            if (boxType == BitsPerComponentBox.BOX_TYPE){
                                final int imageHeaderBoxIndex = getChildIndex(node, ImageHeaderBox.BOX_TYPE);
                                if (imageHeaderBoxIndex!=-1){
                                    final JP2KBox ihBox = (JP2KBox) node.getChildAt(imageHeaderBoxIndex);
                                    final ImageHeaderBox imageHeaderBox = (ImageHeaderBox)LazyJP2KBox.getAsOriginalBox(ihBox);
                                    if (imageHeaderBox.getBitDepth()!=0xFF){
                                        errors.add(new IllegalStateException("BitsPerComponent Box shall not be defined when ImageHeaderBox has BitDepth != 0xFF"));
                                    }
                                }
                            }
                            // //
                            //
                            // Checking the ColorSpecification Box
                            // 
                            // //
                            else if (boxType == ColorSpecificationBox.BOX_TYPE){
                                final ColorSpecificationBox csBox = (ColorSpecificationBox)LazyJP2KBox.getAsOriginalBox(childBox);
                                final byte method = csBox.getMethod();
                                if (method==1){
                                    if (csBox.getICCProfile()!=null){
                                        //TODO: maybe avoid adding an error -> Simply logging a warning
                                        errors.add(new IllegalStateException("ColorSpecification Box with method = 1 should have a NULL ICP"));
                                        
                                    }
                                    final int ecs = csBox.getEnumeratedColorSpace();
                                    if (ecs!=ColorSpecificationBox.ECS_GRAY && ecs!=ColorSpecificationBox.ECS_sRGB &&
                                            ecs!=ColorSpecificationBox.ECS_YCC)
                                        errors.add(new IllegalStateException("Unsupported Enumerated Color Space in ColorSpecification Box"));
                                }
                                else if (method==2){
                                    if (csBox.getEnumeratedColorSpace()!=-1)
                                        errors.add(new IllegalStateException("ColorSpecification Box with method = 2 shouldn't have an Enumerated Color Space defined"));
                                }
                                else
                                    errors.add(new IllegalStateException("ColorSpecification Box only supports value 1 and 2 for the M parameter"));
                           }
                            // //
                            //
                            // Checking the ComponentMapping Box
                            // 
                            // //
                            else if (boxType == ComponentMappingBox.BOX_TYPE){
//                                final ComponentMappingBox cmBox = (ComponentMappingBox) child;
                                //TODO: Add checks
                            }
                            
                            // //
                            //
                            // Checking the Resolution Box
                            // 
                            // //
                            else if (boxType == ResolutionBox.BOX_TYPE){
//                                final ResolutionBox resBox = (ResolutionBox) child;
                                //TODO: Add checks
                            }
                        }
                    }
                }
                break;
                
            case ResolutionBox.BOX_TYPE:
                
            }
            if (errors.size()==0)
                return super.checkNodesInserted(node, childrenIndices, children);
            else 
                return errors;
    }
    }       
    
    
    private Map<JPEG2000FileType,PolicyCheck> policies = new HashMap<JPEG2000FileType, PolicyCheck>();
    
    private DefaultTreeModel model;
    
    private JPEG2000FileType fileType= JPEG2000FileType.UNSPECIFIED;
    
    private Set<JPEG2000FileType> compatibilitySet;

    /**
     * 
     */
    public JP2KTreeController(final DefaultTreeModel model) {
            policies.put(JPEG2000FileType.JP2, new JP2Policy());
            policies.put(JPEG2000FileType.JPX, new JPXPolicy());
            policies.put(JPEG2000FileType.JPXB, new JPXPolicy());
            policies.put(JPEG2000FileType.UNSPECIFIED, new UnspecifiedPolicy());
            
            this.model=model;
    }

    
    /**
     * Return the index of a {@link JP2KBox} child from the specified parent
     * box, given the requested boxType. Return -1 if the child is not
     * found.
     * 
     * @param parentBox
     *                the parent box to be scan.
     * @param boxType
     *                the type specifying the requested box.
     * @return the index of the desired child, -1 if not found.
     */
     int getChildIndex(final JP2KBox parentBox, final int boxType) {
        if (parentBox == null)
            throw new IllegalArgumentException("Specified Parent box is null");
        
        final int numRootChildren = parentBox.getChildCount();
        for (int childN = 0; childN < numRootChildren; childN++) {
            TreeNode childNode = parentBox.getChildAt(childN);
            if (childNode != null && childNode instanceof JP2KBox) {
                JP2KBox childBox = (JP2KBox) parentBox.getChildAt(childN);
                if (childBox.getType() == boxType){
                    return childN;
                }
            }
        }
        return -1;
    }

    /**
     * @see javax.swing.event.TreeModelListener#treeNodesChanged(javax.swing.event.TreeModelEvent)
     */
    public void treeNodesChanged(TreeModelEvent e) {
            System.out.println(e.toString());
    }

    /**
     * @see javax.swing.event.TreeModelListener#treeNodesInserted(javax.swing.event.TreeModelEvent)
     */
    public void treeNodesInserted(TreeModelEvent e) {
            if(e==null)
                    return;
            
            // node to which we have added the children
            final TreePath path = e.getTreePath();
            final Object root = path.getLastPathComponent();
            if(!(root instanceof JP2KBox))
                    throw new IllegalStateException("Node is not a jp2k node");
            final JP2KBox node = (JP2KBox) root;
            final int[] childrenIndices = e.getChildIndices();
            final Object[] children = e.getChildren();
            if (childrenIndices == null || children == null)
                return;

            List<? extends Throwable> exceptions = this.policies.get(this.fileType).checkNodesInserted(node, childrenIndices, children);
            if(LOGGER.isLoggable(Level.SEVERE))
                            for(Throwable t: exceptions)
                                    LOGGER.log(Level.SEVERE,t.getLocalizedMessage(),t);
            if(!exceptions.isEmpty())
                    // TODO Create a specific exception that wraps them all!
                    throw new IllegalStateException("Check failed on this nodes insertion");
    }

    /**
     * @see javax.swing.event.TreeModelListener#treeNodesRemoved(javax.swing.event.TreeModelEvent)
     */
    public void treeNodesRemoved(TreeModelEvent e) {
            System.out.println(e.toString());

    }

    /**
     * @see javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing.event.TreeModelEvent)
     */
    public void treeStructureChanged(TreeModelEvent e) {
            System.out.println(e.toString());
    }

	public void checkTreeConsistency() {
		List<? extends Throwable> exceptions = this.policies.get(this.fileType).checkTreeConsistency();
        if(LOGGER.isLoggable(Level.SEVERE))
                        for(Throwable t: exceptions)
                                LOGGER.log(Level.SEVERE,t.getLocalizedMessage(),t);
        if(!exceptions.isEmpty())
                // TODO Create a specific exception that wraps them all!
                throw new IllegalStateException("Check failed on tree consistency");
		
	}

    
}
