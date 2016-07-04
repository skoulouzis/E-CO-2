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

    public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Tfidf\",\"namespace\":\"tfidf.avro\",\"fields\":[{\"name\":\"document\",\"type\":\"string\"},{\"name\":\"word\",\"type\":\"string\"},{\"name\":\"postDate\",\"type\":\"string\"},{\"name\":\"tf\",\"type\":\"double\"},{\"name\":\"idfArgument\",\"type\":\"double\"},{\"name\":\"tfidfValue\",\"type\":\"string\"}]}");

    public static org.apache.avro.Schema getClassSchema() {
        return SCHEMA$;
    }
    @Deprecated
    public java.lang.CharSequence document;
    @Deprecated
    public java.lang.CharSequence word;
    @Deprecated
    public java.lang.CharSequence postDate;
    @Deprecated
    public double tf;
    @Deprecated
    public double idfArgument;
    @Deprecated
    public java.lang.CharSequence tfidfValue;

    /**
     * Default constructor. Note that this does not initialize fields to their
     * default values from the schema. If that is desired then one should use
     * <code>newBuilder()</code>.
     */
    public Tfidf() {
    }

    /**
     * All-args constructor.
     */
    public Tfidf(java.lang.CharSequence document, java.lang.CharSequence word, java.lang.CharSequence postDate, java.lang.Double tf, java.lang.Double idfArgument, java.lang.CharSequence tfidfValue) {
        this.document = document;
        this.word = word;
        this.postDate = postDate;
        this.tf = tf;
        this.idfArgument = idfArgument;
        this.tfidfValue = tfidfValue;
    }

    public org.apache.avro.Schema getSchema() {
        return SCHEMA$;
    }
    // Used by DatumWriter.  Applications should not call. 

    public java.lang.Object get(int field$) {
        switch (field$) {
            case 0:
                return document;
            case 1:
                return word;
            case 2:
                return postDate;
            case 3:
                return tf;
            case 4:
                return idfArgument;
            case 5:
                return tfidfValue;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }
    // Used by DatumReader.  Applications should not call. 

    @SuppressWarnings(value = "unchecked")
    public void put(int field$, java.lang.Object value$) {
        switch (field$) {
            case 0:
                document = (java.lang.CharSequence) value$;
                break;
            case 1:
                word = (java.lang.CharSequence) value$;
                break;
            case 2:
                postDate = (java.lang.CharSequence) value$;
                break;
            case 3:
                tf = (java.lang.Double) value$;
                break;
            case 4:
                idfArgument = (java.lang.Double) value$;
                break;
            case 5:
                tfidfValue = (java.lang.CharSequence) value$;
                break;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    /**
     * Gets the value of the 'document' field.
     */
    public java.lang.CharSequence getDocument() {
        return document;
    }

    /**
     * Sets the value of the 'document' field.
     *
     * @param value the value to set.
     */
    public void setDocument(java.lang.CharSequence value) {
        this.document = value;
    }

    /**
     * Gets the value of the 'word' field.
     */
    public java.lang.CharSequence getWord() {
        return word;
    }

    /**
     * Sets the value of the 'word' field.
     *
     * @param value the value to set.
     */
    public void setWord(java.lang.CharSequence value) {
        this.word = value;
    }

    /**
     * Gets the value of the 'postDate' field.
     */
    public java.lang.CharSequence getPostDate() {
        return postDate;
    }

    /**
     * Sets the value of the 'postDate' field.
     *
     * @param value the value to set.
     */
    public void setPostDate(java.lang.CharSequence value) {
        this.postDate = value;
    }

    /**
     * Gets the value of the 'tf' field.
     */
    public java.lang.Double getTf() {
        return tf;
    }

    /**
     * Sets the value of the 'tf' field.
     *
     * @param value the value to set.
     */
    public void setTf(java.lang.Double value) {
        this.tf = value;
    }

    /**
     * Gets the value of the 'idfArgument' field.
     */
    public java.lang.Double getIdfArgument() {
        return idfArgument;
    }

    /**
     * Sets the value of the 'idfArgument' field.
     *
     * @param value the value to set.
     */
    public void setIdfArgument(java.lang.Double value) {
        this.idfArgument = value;
    }

    /**
     * Gets the value of the 'tfidfValue' field.
     */
    public java.lang.CharSequence getTfidfValue() {
        return tfidfValue;
    }

    /**
     * Sets the value of the 'tfidfValue' field.
     *
     * @param value the value to set.
     */
    public void setTfidfValue(java.lang.CharSequence value) {
        this.tfidfValue = value;
    }

    /**
     * Creates a new Tfidf RecordBuilder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Creates a new Tfidf RecordBuilder by copying an existing Builder
     */
    public static Builder newBuilder(Builder other) {
        return new Builder(other);
    }

    /**
     * Creates a new Tfidf RecordBuilder by copying an existing Tfidf instance
     */
    public static Builder newBuilder(Tfidf other) {
        return new Builder(other);
    }

    /**
     * RecordBuilder for Tfidf instances.
     */
    public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<Tfidf>
            implements org.apache.avro.data.RecordBuilder<Tfidf> {

        private java.lang.CharSequence document;
        private java.lang.CharSequence word;
        private java.lang.CharSequence postDate;
        private double tf;
        private double idfArgument;
        private java.lang.CharSequence tfidfValue;

        /**
         * Creates a new Builder
         */
        private Builder() {
            super(Tfidf.SCHEMA$);
        }

        /**
         * Creates a Builder by copying an existing Builder
         */
        private Builder(Builder other) {
            super(other);
            if (isValidValue(fields()[0], other.document)) {
                this.document = data().deepCopy(fields()[0].schema(), other.document);
                fieldSetFlags()[0] = true;
            }
            if (isValidValue(fields()[1], other.word)) {
                this.word = data().deepCopy(fields()[1].schema(), other.word);
                fieldSetFlags()[1] = true;
            }
            if (isValidValue(fields()[2], other.postDate)) {
                this.postDate = data().deepCopy(fields()[2].schema(), other.postDate);
                fieldSetFlags()[2] = true;
            }
            if (isValidValue(fields()[3], other.tf)) {
                this.tf = data().deepCopy(fields()[3].schema(), other.tf);
                fieldSetFlags()[3] = true;
            }
            if (isValidValue(fields()[4], other.idfArgument)) {
                this.idfArgument = data().deepCopy(fields()[4].schema(), other.idfArgument);
                fieldSetFlags()[4] = true;
            }
            if (isValidValue(fields()[5], other.tfidfValue)) {
                this.tfidfValue = data().deepCopy(fields()[5].schema(), other.tfidfValue);
                fieldSetFlags()[5] = true;
            }
        }

        /**
         * Creates a Builder by copying an existing Tfidf instance
         */
        private Builder(Tfidf other) {
            super(Tfidf.SCHEMA$);
            if (isValidValue(fields()[0], other.document)) {
                this.document = data().deepCopy(fields()[0].schema(), other.document);
                fieldSetFlags()[0] = true;
            }
            if (isValidValue(fields()[1], other.word)) {
                this.word = data().deepCopy(fields()[1].schema(), other.word);
                fieldSetFlags()[1] = true;
            }
            if (isValidValue(fields()[2], other.postDate)) {
                this.postDate = data().deepCopy(fields()[2].schema(), other.postDate);
                fieldSetFlags()[2] = true;
            }
            if (isValidValue(fields()[3], other.tf)) {
                this.tf = data().deepCopy(fields()[3].schema(), other.tf);
                fieldSetFlags()[3] = true;
            }
            if (isValidValue(fields()[4], other.idfArgument)) {
                this.idfArgument = data().deepCopy(fields()[4].schema(), other.idfArgument);
                fieldSetFlags()[4] = true;
            }
            if (isValidValue(fields()[5], other.tfidfValue)) {
                this.tfidfValue = data().deepCopy(fields()[5].schema(), other.tfidfValue);
                fieldSetFlags()[5] = true;
            }
        }

        /**
         * Gets the value of the 'document' field
         */
        public java.lang.CharSequence getDocument() {
            return document;
        }

        /**
         * Sets the value of the 'document' field
         */
        public Builder setDocument(java.lang.CharSequence value) {
            validate(fields()[0], value);
            this.document = value;
            fieldSetFlags()[0] = true;
            return this;
        }

        /**
         * Checks whether the 'document' field has been set
         */
        public boolean hasDocument() {
            return fieldSetFlags()[0];
        }

        /**
         * Clears the value of the 'document' field
         */
        public Builder clearDocument() {
            document = null;
            fieldSetFlags()[0] = false;
            return this;
        }

        /**
         * Gets the value of the 'word' field
         */
        public java.lang.CharSequence getWord() {
            return word;
        }

        /**
         * Sets the value of the 'word' field
         */
        public Builder setWord(java.lang.CharSequence value) {
            validate(fields()[1], value);
            this.word = value;
            fieldSetFlags()[1] = true;
            return this;
        }

        /**
         * Checks whether the 'word' field has been set
         */
        public boolean hasWord() {
            return fieldSetFlags()[1];
        }

        /**
         * Clears the value of the 'word' field
         */
        public Builder clearWord() {
            word = null;
            fieldSetFlags()[1] = false;
            return this;
        }

        /**
         * Gets the value of the 'postDate' field
         */
        public java.lang.CharSequence getPostDate() {
            return postDate;
        }

        /**
         * Sets the value of the 'postDate' field
         */
        public Builder setPostDate(java.lang.CharSequence value) {
            validate(fields()[2], value);
            this.postDate = value;
            fieldSetFlags()[2] = true;
            return this;
        }

        /**
         * Checks whether the 'postDate' field has been set
         */
        public boolean hasPostDate() {
            return fieldSetFlags()[2];
        }

        /**
         * Clears the value of the 'postDate' field
         */
        public Builder clearPostDate() {
            postDate = null;
            fieldSetFlags()[2] = false;
            return this;
        }

        /**
         * Gets the value of the 'tf' field
         */
        public java.lang.Double getTf() {
            return tf;
        }

        /**
         * Sets the value of the 'tf' field
         */
        public Builder setTf(double value) {
            validate(fields()[3], value);
            this.tf = value;
            fieldSetFlags()[3] = true;
            return this;
        }

        /**
         * Checks whether the 'tf' field has been set
         */
        public boolean hasTf() {
            return fieldSetFlags()[3];
        }

        /**
         * Clears the value of the 'tf' field
         */
        public Builder clearTf() {
            fieldSetFlags()[3] = false;
            return this;
        }

        /**
         * Gets the value of the 'idfArgument' field
         */
        public java.lang.Double getIdfArgument() {
            return idfArgument;
        }

        /**
         * Sets the value of the 'idfArgument' field
         */
        public Builder setIdfArgument(double value) {
            validate(fields()[4], value);
            this.idfArgument = value;
            fieldSetFlags()[4] = true;
            return this;
        }

        /**
         * Checks whether the 'idfArgument' field has been set
         */
        public boolean hasIdfArgument() {
            return fieldSetFlags()[4];
        }

        /**
         * Clears the value of the 'idfArgument' field
         */
        public Builder clearIdfArgument() {
            fieldSetFlags()[4] = false;
            return this;
        }

        /**
         * Gets the value of the 'tfidfValue' field
         */
        public java.lang.CharSequence getTfidfValue() {
            return tfidfValue;
        }

        /**
         * Sets the value of the 'tfidfValue' field
         */
        public Builder setTfidfValue(java.lang.CharSequence value) {
            validate(fields()[5], value);
            this.tfidfValue = value;
            fieldSetFlags()[5] = true;
            return this;
        }

        /**
         * Checks whether the 'tfidfValue' field has been set
         */
        public boolean hasTfidfValue() {
            return fieldSetFlags()[5];
        }

        /**
         * Clears the value of the 'tfidfValue' field
         */
        public Builder clearTfidfValue() {
            tfidfValue = null;
            fieldSetFlags()[5] = false;
            return this;
        }

        @Override
        public Tfidf build() {
            try {
                Tfidf record = new Tfidf();
                record.document = fieldSetFlags()[0] ? this.document : (java.lang.CharSequence) defaultValue(fields()[0]);
                record.word = fieldSetFlags()[1] ? this.word : (java.lang.CharSequence) defaultValue(fields()[1]);
                record.postDate = fieldSetFlags()[2] ? this.postDate : (java.lang.CharSequence) defaultValue(fields()[2]);
                record.tf = fieldSetFlags()[3] ? this.tf : (java.lang.Double) defaultValue(fields()[3]);
                record.idfArgument = fieldSetFlags()[4] ? this.idfArgument : (java.lang.Double) defaultValue(fields()[4]);
                record.tfidfValue = fieldSetFlags()[5] ? this.tfidfValue : (java.lang.CharSequence) defaultValue(fields()[5]);
                return record;
            } catch (Exception e) {
                throw new org.apache.avro.AvroRuntimeException(e);
            }
        }
    }
}
