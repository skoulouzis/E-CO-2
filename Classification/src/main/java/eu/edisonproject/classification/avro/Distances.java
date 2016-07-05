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
package eu.edisonproject.classification.avro;  
/**
 *
 * @author Michele Sparamonti (michele.sparamonti@eng.it)
 */
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class Distances extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Distances\",\"namespace\":\"distances.avro\",\"fields\":[{\"name\":\"documentId\",\"type\":\"string\"},{\"name\":\"date\",\"type\":\"string\"},{\"name\":\"distances\",\"type\":{\"type\":\"array\",\"items\":\"double\"}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.lang.CharSequence documentId;
  @Deprecated public java.lang.CharSequence date;
  @Deprecated public java.util.List<java.lang.Double> distances;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public Distances() {}

  /**
   * All-args constructor.
   */
  public Distances(java.lang.CharSequence documentId, java.lang.CharSequence date, java.util.List<java.lang.Double> distances) {
    this.documentId = documentId;
    this.date = date;
    this.distances = distances;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return documentId;
    case 1: return date;
    case 2: return distances;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: documentId = (java.lang.CharSequence)value$; break;
    case 1: date = (java.lang.CharSequence)value$; break;
    case 2: distances = (java.util.List<java.lang.Double>)value$; break;
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
   * Gets the value of the 'date' field.
   */
  public java.lang.CharSequence getDate() {
    return date;
  }

  /**
   * Sets the value of the 'date' field.
   * @param value the value to set.
   */
  public void setDate(java.lang.CharSequence value) {
    this.date = value;
  }

  /**
   * Gets the value of the 'distances' field.
   */
  public java.util.List<java.lang.Double> getDistances() {
    return distances;
  }

  /**
   * Sets the value of the 'distances' field.
   * @param value the value to set.
   */
  public void setDistances(java.util.List<java.lang.Double> value) {
    this.distances = value;
  }

  /** Creates a new Distances RecordBuilder */
  public static Distances.Builder newBuilder() {
    return new Distances.Builder();
  }
  
  /** Creates a new Distances RecordBuilder by copying an existing Builder */
  public static Distances.Builder newBuilder(Distances.Builder other) {
    return new Distances.Builder(other);
  }
  
  /** Creates a new Distances RecordBuilder by copying an existing Distances instance */
  public static Distances.Builder newBuilder(Distances other) {
    return new Distances.Builder(other);
  }
  
  /**
   * RecordBuilder for Distances instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<Distances>
    implements org.apache.avro.data.RecordBuilder<Distances> {

    private java.lang.CharSequence documentId;
    private java.lang.CharSequence date;
    private java.util.List<java.lang.Double> distances;

    /** Creates a new Builder */
    private Builder() {
      super(Distances.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(Distances.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.documentId)) {
        this.documentId = data().deepCopy(fields()[0].schema(), other.documentId);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.date)) {
        this.date = data().deepCopy(fields()[1].schema(), other.date);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.distances)) {
        this.distances = data().deepCopy(fields()[2].schema(), other.distances);
        fieldSetFlags()[2] = true;
      }
    }
    
    /** Creates a Builder by copying an existing Distances instance */
    private Builder(Distances other) {
            super(Distances.SCHEMA$);
      if (isValidValue(fields()[0], other.documentId)) {
        this.documentId = data().deepCopy(fields()[0].schema(), other.documentId);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.date)) {
        this.date = data().deepCopy(fields()[1].schema(), other.date);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.distances)) {
        this.distances = data().deepCopy(fields()[2].schema(), other.distances);
        fieldSetFlags()[2] = true;
      }
    }

    /** Gets the value of the 'documentId' field */
    public java.lang.CharSequence getDocumentId() {
      return documentId;
    }
    
    /** Sets the value of the 'documentId' field */
    public Distances.Builder setDocumentId(java.lang.CharSequence value) {
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
    public Distances.Builder clearDocumentId() {
      documentId = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'date' field */
    public java.lang.CharSequence getDate() {
      return date;
    }
    
    /** Sets the value of the 'date' field */
    public Distances.Builder setDate(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.date = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'date' field has been set */
    public boolean hasDate() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'date' field */
    public Distances.Builder clearDate() {
      date = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'distances' field */
    public java.util.List<java.lang.Double> getDistances() {
      return distances;
    }
    
    /** Sets the value of the 'distances' field */
    public Distances.Builder setDistances(java.util.List<java.lang.Double> value) {
      validate(fields()[2], value);
      this.distances = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'distances' field has been set */
    public boolean hasDistances() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'distances' field */
    public Distances.Builder clearDistances() {
      distances = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    public Distances build() {
      try {
        Distances record = new Distances();
        record.documentId = fieldSetFlags()[0] ? this.documentId : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.date = fieldSetFlags()[1] ? this.date : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.distances = fieldSetFlags()[2] ? this.distances : (java.util.List<java.lang.Double>) defaultValue(fields()[2]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
