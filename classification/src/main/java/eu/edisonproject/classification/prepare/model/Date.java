/*
 * Copyright 2016 Michele Sparamonti & Spiros Koulouzis.
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
package eu.edisonproject.classification.prepare.model;

//import java.time.LocalDate;
import org.joda.time.LocalDate;

//import java.time.format.DateTimeParseException;

/*
 * @author Michele Sparamonti
 */
public class Date extends Extractor {

  LocalDate date;

  public Date() {
    date = LocalDate.now();
  }

  @Override
  public void extract() {
    if (this.getJp().getDescription().contains(" date") == true) {
      int start = this.getJp().getDescription().indexOf(" date");
      date = LocalDate.parse(this.getJp().getDescription().substring(start, start + 50));
    }

    this.getJp().setDate(date);
  }

}
