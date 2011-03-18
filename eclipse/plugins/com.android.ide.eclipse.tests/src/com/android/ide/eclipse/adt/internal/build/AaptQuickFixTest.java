/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ide.eclipse.adt.internal.build;

import static com.android.AndroidConstants.FD_RES_LAYOUT;
import static com.android.sdklib.SdkConstants.FD_RES;

import com.android.ide.eclipse.adt.AdtConstants;
import com.android.ide.eclipse.adt.AdtPlugin;
import com.android.ide.eclipse.adt.internal.editors.AndroidXmlEditor;
import com.android.ide.eclipse.adt.internal.editors.layout.refactoring.AdtProjectTest;
import com.android.ide.eclipse.adt.internal.editors.xml.Hyperlinks;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AaptQuickFixTest extends AdtProjectTest {
    public void testQuickFix1() throws Exception {
        // Test adding a value into an existing file (res/values/strings.xml)
        checkFixes("quickfix1.xml", "android:text=\"@string/firs^tstring\"",
                "res/values/strings.xml");
    }

    public void testQuickFix2() throws Exception {
        // Test adding a value into a new file (res/values/dimens.xml, will be created)
        checkFixes("quickfix1.xml", "android:layout_width=\"@dimen/^testdimen\"",
                "res/values/dimens.xml");
    }

    public void testQuickFix3() throws Exception {
        // Test adding a file based resource (uses new file wizard machinery)
        checkFixes("quickfix1.xml", "layout=\"@layout/^testlayout\"", "res/layout/testlayout.xml");
    }

    private void checkFixes(String name, String caretLocation, String expectedNewPath)
            throws Exception {
        IProject project = getProject();
        IFile file = getTestDataFile(project, name, FD_RES + "/" + FD_RES_LAYOUT + "/" + name);

        // Determine the offset
        final int offset = getCaretOffset(file, caretLocation);


        // Run AaptParser such that markers are added...
        // When debugging these tests, the project gets a chance to build itself so
        // the real aapt errors are there. But when the test is run directly, aapt has
        // not yet run. I tried waiting for the build (using the code in SampleProjectTest)
        // but this had various adverse effects (exception popups from the Eclipse debugger
        // etc) so instead this test just hardcodes the aapt errors that should be
        // observed on quickfix1.xml.
        assertEquals("Unit test is hardcoded to errors for quickfix1.xml", "quickfix1.xml", name);
        String osRoot = project.getLocation().toOSString();
        List<String> errors = new ArrayList<String>();
        String fileRelativePath = file.getProjectRelativePath().toPortableString();
        String filePath = osRoot + File.separator + fileRelativePath;
        errors.add(filePath + ":7: error: Error: No resource found that matches the given name"
                + " (at 'text' with value '@string/firststring').");
        errors.add(filePath + ":7: error: Error: No resource found that matches the given name"
                + " (at 'layout_width' with value '@dimen/testdimen').");
        errors.add(filePath + ":13: error: Error: No resource found that matches the given name"
                + " (at 'layout' with value '@layout/testlayout').");
        AaptParser.parseOutput(errors, project);

        AaptQuickFix aaptQuickFix = new AaptQuickFix();

        // Open file
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        assertNotNull(page);
        IEditorPart editor = IDE.openEditor(page, file);
        assertTrue(editor instanceof AndroidXmlEditor);
        AndroidXmlEditor layoutEditor = (AndroidXmlEditor) editor;
        final ISourceViewer viewer = layoutEditor.getStructuredSourceViewer();

        // Test marker resolution.
         IMarker[] markers = file.findMarkers(AdtConstants.MARKER_AAPT_COMPILE, true,
                 IResource.DEPTH_ZERO);
         for (IMarker marker : markers) {
             int start = marker.getAttribute(IMarker.CHAR_START, 0);
             int end = marker.getAttribute(IMarker.CHAR_END, 0);
             if (offset >= start && offset <= end) {
                 // Found the target marker. Now check the marker resolution of it.
                 assertTrue(aaptQuickFix.hasResolutions(marker));
                 IMarkerResolution[] resolutions = aaptQuickFix.getResolutions(marker);
                 assertNotNull(resolutions);
                 assertEquals(1, resolutions.length);
                 IMarkerResolution resolution = resolutions[0];
                 assertNotNull(resolution);
                 assertTrue(resolution.getLabel().contains("Create resource"));

                 // Not running marker yet -- if we create the files here they already
                 // exist when the quick assist code runs. (The quick fix and the quick assist
                 // mostly share code for the implementation anyway.)
                 //resolution.run(marker);
                 break;
             }
         }

        // Next test quick assist.

        IQuickAssistInvocationContext invocationContext = new IQuickAssistInvocationContext() {
            public int getLength() {
                return 0;
            }

            public int getOffset() {
                return offset;
            }

            public ISourceViewer getSourceViewer() {
                return viewer;
            }
        };
        ICompletionProposal[] proposals = aaptQuickFix
                .computeQuickAssistProposals(invocationContext);
        assertNotNull(proposals);
        assertTrue(proposals.length == 1);
        ICompletionProposal proposal = proposals[0];

        assertNotNull(proposal.getAdditionalProposalInfo());
        assertNotNull(proposal.getImage());
        assertTrue(proposal.getDisplayString().contains("Create resource"));

        IDocument document = new Document();
        String fileContent = AdtPlugin.readFile(file);
        document.set(fileContent);

        // Apply quick fix
        proposal.apply(document);

        IPath path = new Path(expectedNewPath);
        IFile newFile = project.getFile(path);
        assertNotNull(path.toPortableString(), newFile);

        // Ensure that the newly created file was opened
        IEditorPart currentFile = Hyperlinks.getEditor();
        assertEquals(newFile.getProjectRelativePath(),
             ((FileEditorInput) currentFile.getEditorInput()).getFile().getProjectRelativePath());

        // Look up caret offset
        assertTrue(currentFile instanceof AndroidXmlEditor);
        AndroidXmlEditor newEditor = (AndroidXmlEditor) currentFile;
        ISourceViewer newViewer = newEditor.getStructuredSourceViewer();
        Point selectedRange = newViewer.getSelectedRange();

        String newFileContents = AdtPlugin.readFile(newFile);

        // Insert selection markers -- [ ] for the selection range, ^ for the caret
        String newFileWithCaret;
        int selectionBegin = selectedRange.x;
        int selectionEnd = selectionBegin + selectedRange.y;
        if (selectionBegin < selectionEnd) {
            newFileWithCaret = newFileContents.substring(0, selectionBegin) + "[^"
                    + newFileContents.substring(selectionBegin, selectionEnd) + "]"
                    + newFileContents.substring(selectionEnd);
        } else {
            // Selected range
            newFileWithCaret = newFileContents.substring(0, selectionBegin) + "^"
                    + newFileContents.substring(selectionBegin);
        }

        newFileWithCaret = removeSessionData(newFileWithCaret);

        assertEqualsGolden(name, newFileWithCaret);
    }
}
