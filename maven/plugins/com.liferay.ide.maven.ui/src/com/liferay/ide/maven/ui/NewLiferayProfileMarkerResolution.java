/*******************************************************************************
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 *******************************************************************************/

package com.liferay.ide.maven.ui;

import com.liferay.ide.maven.core.LiferayMavenCore;
import com.liferay.ide.maven.core.MavenUtil;
import com.liferay.ide.project.core.model.NewLiferayPluginProjectOp;
import com.liferay.ide.project.core.model.NewLiferayProfile;
import com.liferay.ide.project.ui.action.NewLiferayProfileActionHandler;
import com.liferay.ide.project.ui.wizard.NewLiferayPluginProjectWizard;
import com.liferay.ide.ui.util.UIUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.sapphire.ui.def.DefinitionLoader;
import org.eclipse.sapphire.ui.def.DefinitionLoader.Reference;
import org.eclipse.sapphire.ui.forms.DialogDef;
import org.eclipse.sapphire.ui.forms.swt.SapphireDialog;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.wst.xml.core.internal.provisional.format.FormatProcessorXML;
import org.w3c.dom.Node;

/**
 * @author Gregory Amerson
 */
@SuppressWarnings( "restriction" )
public class NewLiferayProfileMarkerResolution extends AbstractProjectMarkerResolution
{

    private String pluginVersion;

    public NewLiferayProfileMarkerResolution( final String pluginVersion )
    {
        super();

        this.pluginVersion = pluginVersion;
    }

    public String getLabel()
    {
        return "Create a new maven profile based on a Liferay runtime and attach it to the project.";
    }

    @Override
    protected int promptUser( IProject project, NewLiferayPluginProjectOp op )
    {
        final NewLiferayProfile newLiferayProfile = op.getNewLiferayProfiles().insert();

        final Reference<DialogDef> dialogRef = DefinitionLoader.sdef(
            NewLiferayPluginProjectWizard.class ).dialog( "NewLiferayProfile" );
        final SapphireDialog dialog = new SapphireDialog( UIUtil.getActiveShell(), newLiferayProfile, dialogRef );

        dialog.setBlockOnOpen( true );
        final int result = dialog.open();

        if( result == SapphireDialog.OK )
        {
            try
            {
                final IFile pomFile = project.getFile( IMavenConstants.POM_FILE_NAME );

                final IDOMModel domModel =
                    (IDOMModel) StructuredModelManager.getModelManager().getModelForEdit( pomFile );

                final Node newNode =
                    MavenUtil.createNewLiferayProfileNode(
                        domModel.getDocument(), newLiferayProfile, this.pluginVersion );

                final FormatProcessorXML formatter = new FormatProcessorXML();
                formatter.formatNode( newNode );

                domModel.save();

                domModel.releaseFromEdit();
            }
            catch( Exception e )
            {
                LiferayMavenCore.logError( "Unable to save new Liferay profiles to project pom.", e );
            }

            NewLiferayProfileActionHandler.addToActiveProfiles( op, newLiferayProfile );
        }
        else
        {
            op.getNewLiferayProfiles().remove( newLiferayProfile );
        }

        return result;
    }

}
