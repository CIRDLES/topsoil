package org.cirdles.topsoil.app.model;

import org.cirdles.topsoil.app.model.generic.BranchNode;
import org.cirdles.topsoil.app.model.generic.DataNode;

/**
 * @author marottajb
 */
public class DataCategory extends BranchNode<DataNode> {

    //**********************************************//
    //                  CONSTANTS                   //
    //**********************************************//

    private static final long serialVersionUID = 372814634643053288L;

    //**********************************************//
    //                 CONSTRUCTORS                 //
    //**********************************************//

    public DataCategory(String label, DataNode... children) {
        super(label, children);
    }

}