/*
 * Copyright 2016 Michele Sparamonti & Spiros Koulouzis
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
package eu.edisonproject.training.tfidf.avro;  

/**
 *
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
 */
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class Tfidf extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Tfidf\",\"namespace\":\"tfidf.avro\",\"fields\":[{\"name\":\"documentId\",\"type\":\"string\"},{\"name\":\"word\",\"type\":\"string\"},{\"name\":\"tfidf\",\"type\":\"string\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.lang.CharSequence documentId;
  @Deprecated public java.lang.CharSequence word;
  @Deprecated public java.lang.CharSequence tfidf;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public Tfidf() {}

  /**
   * All-args constructor.
   */
  public Tfidf(java.lang.CharSequence documentId, java.lang.CharSequence word, java.lang.CharSequence tfidf) {
    this.documentId = documentId;
    this.word = word;
    this.tfidf = tfidf;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return documentId;
    case 1: return word;
    case 2: return tfidf;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: documentId = (java.lang.CharSequence)value$; break;
    case 1: word = (java.lang.CharSequence)value$; break;
    case 2: tfidf = (java.lang.CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'documentId' field.
   */
  public java.lang.CharSequence getDocumentId() {
    return documentId;
  }

  /**
   * Sets the value of the 'documentId' field.
   * @param value the value to set.
   */
  public void setDocumentId(java.lang.CharSequence value) {
    this.documentId = value;
  }

  /**
   * Gets the value of the 'word' field.
   */
  public java.lang.CharSequence getWord() {
    return word;
  }

  /**
   * Sets the value of the 'word' field.
   * @param value the value to set.
   */
  public void setWord(java.lang.CharSequence value) {
    this.word = value;
  }

  /**
   * Gets the value of the 'tfidf' field.
   */
  public java.lang.CharSequence getTfidf() {
    return tfidf;
  }

  /**
   * Sets the value of the 'tfidf' field.
   * @param value the value to set.
   */
  public void setTfidf(java.lang.CharSequence value) {
    this.tfidf = value;
  }

  /** Creates a new Tfidf RecordBuilder */
  public static Tfidf.Builder newBuilder() {
    return new Tfidf.Builder();
  }
  
  /** Creates a new Tfidf RecordBuilder by copying an existing Builder */
  public static Tfidf.Builder newBuilder(Tfidf.Builder other) {
    return new Tfidf.Builder(other);
  }
  
  /** Creates a new Tfidf RecordBuilder by copying an existing Tfidf instance */
  public static Tfidf.Builder newBuilder(Tfidf other) {
    return new Tfidf.Builder(other);
  }
  
  /**
   * RecordBuilder for Tfidf instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<Tfidf>
    implements org.apache.avro.data.RecordBuilder<Tfidf> {

    private java.lang.CharSequence documentId;
    private java.lang.CharSequence word;
    private java.lang.CharSequence tfidf;

    /** Creates a new Builder */
    private Builder() {
      super(Tfidf.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(Tfidf.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.documentId)) {
        this.documentId = data().deepCopy(fields()[0].schema(), other.documentId);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.word)) {
        this.word = data().deepCopy(fields()[1].schema(), other.word);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.tfidf)) {
        this.tfidf = data().deepCopy(fields()[2].schema(), other.tfidf);
        fieldSetFlags()[2] = true;
      }
    }
    
    /** Creates a Builder by copying an existing Tfidf instance */
    private Builder(Tfidf other) {
            super(Tfidf.SCHEMA$);
      if (isValidValue(fields()[0], other.documentId)) {
        this.documentId = data().deepCopy(fields()[0].schema(), other.documentId);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.word)) {
        this.word = data().deepCopy(fields()[1].schema(), other.word);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.tfidf)) {
        this.tfidf = data().deepCopy(fields()[2].schema(), other.tfidf);
        fieldSetFlags()[2] = true;
      }
    }

    /** Gets the value of the 'documentId' field */
    public java.lang.CharSequence getDocumentId() {
      return documentId;
    }
    
    /** Sets the value of the 'documentId' field */
    public Tfidf.Builder setDocumentId(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.documentId = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'documentId' field has been set */
    public boolean hasDocumentId() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'documentId' field */
    public Tfidf.Builder clearDocumentId() {
      documentId = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'word' field */
    public java.lang.CharSequence getWord() {
      return word;
    }
    
    /** Sets the value of the 'word' field */
    public Tfidf.Builder setWord(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.word = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'word' field has been set */
    public boolean hasWord() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'word' field */
    public Tfidf.Builder clearWord() {
      word = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'tfidf' field */
    public java.lang.CharSequence getTfidf() {
      return tfidf;
    }
    
    /** Sets the value of the 'tfidf' field */
    public Tfidf.Builder setTfidf(java.lang.CharSequence value) {
      validate(fields()[2], value);
      this.tfidf = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'tfidf' field has been set */
    public boolean hasTfidf() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'tfidf' field */
    public Tfidf.Builder clearTfidf() {
      tfidf = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    public Tfidf build() {
      try {
        Tfidf record = new Tfidf();
        record.documentId = fieldSetFlags()[0] ? this.documentId : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.word = fieldSetFlags()[1] ? this.word : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.tfidf = fieldSetFlags()[2] ? this.tfidf : (java.lang.CharSequence) defaultValue(fields()[2]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
