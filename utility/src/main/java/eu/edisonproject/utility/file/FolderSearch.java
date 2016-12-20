/*
 * Copyright 2016 S. Koulouzis.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.edisonproject.utility.file;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author S. Koulouzis
 */
public class FolderSearch {

  private final String searchName;
  private final Set<String> result = new HashSet<>();
  private final boolean unique;
  private final File startDir;

  public FolderSearch(File startDir, String searchName, boolean unique) {
    this.searchName = searchName;
    this.unique = unique;
    this.startDir = startDir;
  }

  public Set<String> search() throws IOException {
    if (startDir.isDirectory()) {
      search(startDir);
    } else {
      throw new IOException(startDir.getAbsolutePath() + " is not a folder");
    }
    return result;
  }

  private void search(File file) {
    if (unique && result.size() >= 1) {
      return;
    }
    if (file.isDirectory()) {
      for (File tmp : file.listFiles()) {
        if (tmp.isDirectory() && !tmp.getName().equals(searchName)) {
          search(tmp);
        } else if (tmp.getName().equals(searchName)) {
          result.add(tmp.getAbsolutePath());
          if (unique) {
            break;
          }
        }
      }
    }

  }

}
