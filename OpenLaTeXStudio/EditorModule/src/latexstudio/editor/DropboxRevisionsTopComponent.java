/* 
 * Copyright (c) 2015 Sebastian Brudzinski
 * 
 * See the file LICENSE for copying permission.
 */
package latexstudio.editor;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import latexstudio.editor.remote.DbxEntryRevision;
import latexstudio.editor.remote.DbxState;
import latexstudio.editor.remote.DbxUtil;
import latexstudio.editor.util.ApplicationUtils;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.io.IOUtils;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component that displays Dropbox file revisions.
 */
@ConvertAsProperties(
        dtd = "-//latexstudio.editor//DropboxRevisions//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "DropboxRevisionsTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "latexstudio.editor.DropboxRevisionsTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_DropboxRevisionsAction",
        preferredID = "DropboxRevisionsTopComponent"
)
@Messages({
    "CTL_DropboxRevisionsAction=Dropbox Revisions",
    "CTL_DropboxRevisionsTopComponent=Dropbox Revisions",
    "HINT_DropboxRevisionsTopComponent=This is a Dropbox Revisions window"
})
public final class DropboxRevisionsTopComponent extends TopComponent {
    
    private DefaultListModel<DbxEntryRevision> dlm = new DefaultListModel<DbxEntryRevision>();
    private final ApplicationLogger LOGGER = new ApplicationLogger("Dropbox");
    
    private final RevisionDisplayTopComponent revtc = new TopComponentFactory<RevisionDisplayTopComponent>()
            .getTopComponent(RevisionDisplayTopComponent.class.getSimpleName());

    public DropboxRevisionsTopComponent() {
        initComponents();
        setName(Bundle.CTL_DropboxRevisionsTopComponent());
        setToolTipText(Bundle.HINT_DropboxRevisionsTopComponent());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();

        jList1.setModel(dlm);
        jList1.setToolTipText(org.openide.util.NbBundle.getMessage(DropboxRevisionsTopComponent.class, "DropboxRevisionsTopComponent.jList1.toolTipText")); // NOI18N
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
        if (evt.getClickCount() == 2) {
            DbxEntryRevision entry = (DbxEntryRevision) jList1.getSelectedValue();
            DbxClient client = DbxUtil.getDbxClient();
            
            FileOutputStream outputStream = null;
            File outputFile = new File(ApplicationUtils.getAppDirectory() + File.separator + entry.getName() + entry.getRevision());

            try {
                outputStream = new FileOutputStream(outputFile);
                client.getFile(entry.getPath(), entry.getRevision(), outputStream);
                LOGGER.log("Loaded revision " + entry.getRevision() + " from Dropbox");
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (DbxException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                IOUtils.closeQuietly(outputStream);
            }

            revtc.open();
            revtc.requestActive();
            revtc.setName(entry.getName() + " (rev: " + entry.getRevision() + ")");
            revtc.setDisplayedRevision(new DbxState(entry.getPath(), entry.getRevision()));
            try {
                revtc.setText(FileUtils.readFileToString(outputFile));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            updateRevisionsList(entry.getPath());
        }
    }//GEN-LAST:event_jList1MouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
    
    public void updateRevisionsList(String path) {
        DbxClient client = DbxUtil.getDbxClient();
        List<DbxEntry.File> entries = null;
         
        try {
            entries = client.getRevisions(path);
        } catch (DbxException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        dlm.clear();
        for (DbxEntry.File dbxEntry : entries) {
            dlm.addElement(new DbxEntryRevision(dbxEntry));
        }
    }

    public JList getjList1() {
        return jList1;
    }

    public void setjList1(JList jList1) {
        this.jList1 = jList1;
    }

    public DefaultListModel<DbxEntryRevision> getDlm() {
        return dlm;
    }

    public void setDlm(DefaultListModel<DbxEntryRevision> dlm) {
        this.dlm = dlm;
    }
    
}
