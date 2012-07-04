/**
 * [程序名称] Android 文件浏览器 <br>
 * [作者] xmobileapp团队 <br>
 * [参考资料] www.anddev.org <br>
 * [开源协议] Apache License, Version 2.0<br>
 * (http://www.apache.org/licenses/LICENSE-2.0) Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License. <br>
 */
/**
 * Modify：人人IT网<br>
 * Site: www.renren.it
 */
package com.xmobileapp.filebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.xmobileapp.filebrowser.iconifiedlist.IconifiedText;
import com.xmobileapp.filebrowser.iconifiedlist.IconifiedTextListAdapter;

public class FileBrowser extends ListActivity {

    private enum DISPLAYMODE {
        ABSOLUTE, RELATIVE;
    }

    protected static final int  SUB_ACTIVITY_REQUEST_CODE = 1337;

    private final DISPLAYMODE   displayMode               = DISPLAYMODE.RELATIVE;
    private List<IconifiedText> directoryEntries          = new ArrayList<IconifiedText>();
    private File                currentDirectory          = new File("/");

    /** Activity加载时调用 */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setTheme(android.R.style.Theme_Black);
        browseToRoot();
        this.setSelection(0);
    }

    /**
     * This function browses to the root-directory of the file-system.
     */
    private void browseToRoot() {
        browseTo(new File("/"));
    }

    /**
     * This function browses up one level according to the field: currentDirectory
     */
    private void upOneLevel() {
        if (this.currentDirectory.getParent() != null) this.browseTo(this.currentDirectory.getParentFile());
    }

    private void browseTo(final File aDirectory) {
        // On relative we display the full path in the title.
        if (this.displayMode == DISPLAYMODE.RELATIVE) this.setTitle(aDirectory.getAbsolutePath() + " :: "
                                                                    + getString(R.string.app_name));
        if (aDirectory.isDirectory()) {
            this.currentDirectory = aDirectory;
            fill(aDirectory.listFiles());
        } else {
            // Show an Alert with the ButtonListeners we created
            new AlertDialog.Builder(this).setTitle("title").setMessage("message").create().show();
        }
    }

    private void fill(File[] files) {
        this.directoryEntries.clear();
        // Add the "." == "current directory"
        this.directoryEntries.add(new IconifiedText(getString(R.string.current_dir),
                                                    getResources().getDrawable(R.drawable.folder)));
        // and the ".." == 'Up one level'
        if (this.currentDirectory.getParent() != null) {
            this.directoryEntries.add(new IconifiedText(getString(R.string.up_one_level),
                                                        getResources().getDrawable(R.drawable.uponelevel)));
        }
        Drawable currentIcon = null;
        if (files != null) {
            for (File currentFile : files) {
                if (currentFile.isDirectory()) {
                    currentIcon = getResources().getDrawable(R.drawable.folder);
                } else {
                    String fileName = currentFile.getName();
                    /*
                     * Determine the Icon to be used, depending on the FileEndings defined in:
                     * res/values/fileendings.xml.
                     */
                    if (checkEndsWithInStringArray(fileName, getResources().getStringArray(R.array.fileEndingImage))) {
                        currentIcon = getResources().getDrawable(R.drawable.image);
                    } else if (checkEndsWithInStringArray(fileName,
                                                          getResources().getStringArray(R.array.fileEndingWebText))) {
                        currentIcon = getResources().getDrawable(R.drawable.webtext);
                    } else if (checkEndsWithInStringArray(fileName,
                                                          getResources().getStringArray(R.array.fileEndingPackage))) {
                        currentIcon = getResources().getDrawable(R.drawable.packed);
                    } else if (checkEndsWithInStringArray(fileName,
                                                          getResources().getStringArray(R.array.fileEndingAudio))) {
                        currentIcon = getResources().getDrawable(R.drawable.audio);
                    } else {
                        currentIcon = getResources().getDrawable(R.drawable.text);
                    }
                }
                switch (this.displayMode) {
                    case ABSOLUTE:
                        /* On absolute Mode, we show the full path */
                        this.directoryEntries.add(new IconifiedText(currentFile.getAbsolutePath(), currentIcon));
                        break;
                    case RELATIVE:
                        this.directoryEntries.add(new IconifiedText(currentFile.getName(), currentIcon));
                        break;
                }
            }
        } else {

        }
        Collections.sort(this.directoryEntries);

        IconifiedTextListAdapter itla = new IconifiedTextListAdapter(this);
        itla.setListItems(this.directoryEntries);
        this.setListAdapter(itla);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        int selectionRowID = position;
        String selectedFileString = this.directoryEntries.get(selectionRowID).getText();
        if (selectedFileString.equals(getString(R.string.current_dir))) {
            // Refresh
            this.browseTo(this.currentDirectory);
        } else if (selectedFileString.equals(getString(R.string.up_one_level))) {
            this.upOneLevel();
        } else {
            File clickedFile = null;
            switch (this.displayMode) {
                case RELATIVE:
                    String path = this.currentDirectory.getAbsolutePath();
                    if (!path.endsWith("/")) {
                        path += "/";
                    }
                    path += selectedFileString;
                    clickedFile = new File(path);
                    break;
                case ABSOLUTE:
                    clickedFile = new File(selectedFileString);
                    break;
            }
            if (clickedFile != null && clickedFile.isDirectory()) {
                this.browseTo(clickedFile);
            } else {
                // 生成一个Intent对象
                Intent intent = new Intent(this, TextViewActivity.class);
                // 设置传递的参数
                intent.putExtra("filePath", clickedFile.getAbsolutePath());
                startActivity(intent);
            }
        }
    }

    /**
     * Checks whether checkItsEnd ends with one of the Strings from fileEndings
     */
    private boolean checkEndsWithInStringArray(String checkItsEnd, String[] fileEndings) {
        for (String aEnd : fileEndings) {
            if (checkItsEnd.endsWith(aEnd)) return true;
        }
        return false;
    }
}
