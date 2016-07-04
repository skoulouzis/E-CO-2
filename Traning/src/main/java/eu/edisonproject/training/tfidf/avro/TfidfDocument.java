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
public class TfidfDocument extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {

    public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"TfidfDocument\",\"namespace\":\"tfidfDocument.avro\",\"fields\":[{\"name\":\"title\",\"type\":\"string\"},{\"name\":\"word\",\"type\":\"string\"},{\"name\":\"postDate\",\"type\":\"string\"},{\"name\":\"tfidfValue\",\"type\":\"string\"}]}");

    public static org.apache.avro.Schema getClassSchema() {
        return SCHEMA$;
    }
    @Deprecated
    public java.lang.CharSequence title;
    @Deprecated
    public java.lang.CharSequence word;
    @Deprecated
    public java.lang.CharSequence postDate;
    @Deprecated
    public java.lang.CharSequence tfidfValue;

    /**
     * Default constructor. Note that this does not initialize fields to their
     * default values from the schema. If that is desired then one should use
     * <code>newBuilder()</code>.
     */
    public TfidfDocument() {
    }

    /**
     * All-args constructor.
     */
    public TfidfDocument(java.lang.CharSequence title, java.lang.CharSequence word, java.lang.CharSequence postDate, java.lang.CharSequence tfidfValue) {
        this.title = title;
        this.word = word;
        this.postDate = postDate;
        this.tfidfValue = tfidfValue;
    }

    public org.apache.avro.Schema getSchema() {
        return SCHEMA$;
    }
    // Used by DatumWriter.  Applications should not call. 

    public java.lang.Object get(int field$) {
        switch (field$) {
            case 0:
                return title;
            case 1:
                return word;
            case 2:
                return postDate;
            case 3:
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
                title = (java.lang.CharSequence) value$;
                break;
            case 1:
                word = (java.lang.CharSequence) value$;
                break;
            case 2:
                postDate = (java.lang.CharSequence) value$;
                break;
            case 3:
                tfidfValue = (java.lang.CharSequence) value$;
                break;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    /**
     * Gets the value of the 'title' field.
     */
    public java.lang.CharSequence getTitle() {
        return title;
    }

    /**
     * Sets the value of the 'title' field.
     *
     * @param value the value to set.
     */
    public void setTitle(java.lang.CharSequence value) {
        this.title = value;
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
     * Creates a new TfidfDocument RecordBuilder
     */
    public static TfidfDocument.Builder newBuilder() {
        return new TfidfDocument.Builder();
    }

    /**
     * Creates a new TfidfDocument RecordBuilder by copying an existing Builder
     */
    public static TfidfDocument.Builder newBuilder(TfidfDocument.Builder other) {
        return new TfidfDocument.Builder(other);
    }

    /**
     * Creates a new TfidfDocument RecordBuilder by copying an existing
     * TfidfDocument instance
     */
    public static TfidfDocument.Builder newBuilder(TfidfDocument other) {
        return new TfidfDocument.Builder(other);
    }

    /**
     * RecordBuilder for TfidfDocument instances.
     */
    public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<TfidfDocument>
            implements org.apache.avro.data.RecordBuilder<TfidfDocument> {

        private java.lang.CharSequence title;
        private java.lang.CharSequence word;
        private java.lang.CharSequence postDate;
        private java.lang.CharSequence tfidfValue;

        /**
         * Creates a new Builder
         */
        private Builder() {
            super(TfidfDocument.SCHEMA$);
        }

        /**
         * Creates a Builder by copying an existing Builder
         */
        private Builder(TfidfDocument.Builder other) {
            super(other);
            if (isValidValue(fields()[0], other.title)) {
                this.title = data().deepCopy(fields()[0].schema(), other.title);
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
            if (isValidValue(fields()[3], other.tfidfValue)) {
                this.tfidfValue = data().deepCopy(fields()[3].schema(), other.tfidfValue);
                fieldSetFlags()[3] = true;
            }
        }

        /**
         * Creates a Builder by copying an existing TfidfDocument instance
         */
        private Builder(TfidfDocument other) {
            super(TfidfDocument.SCHEMA$);
            if (isValidValue(fields()[0], other.title)) {
                this.title = data().deepCopy(fields()[0].schema(), other.title);
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
            if (isValidValue(fields()[3], other.tfidfValue)) {
                this.tfidfValue = data().deepCopy(fields()[3].schema(), other.tfidfValue);
                fieldSetFlags()[3] = true;
            }
        }

        /**
         * Gets the value of the 'title' field
         */
        public java.lang.CharSequence getTitle() {
            return title;
        }

        /**
         * Sets the value of the 'title' field
         */
        public TfidfDocument.Builder setTitle(java.lang.CharSequence value) {
            validate(fields()[0], value);
            this.title = value;
            fieldSetFlags()[0] = true;
            return this;
        }

        /**
         * Checks whether the 'title' field has been set
         */
        public boolean hasTitle() {
            return fieldSetFlags()[0];
        }

        /**
         * Clears the value of the 'title' field
         */
        public TfidfDocument.Builder clearTitle() {
            title = null;
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
        public TfidfDocument.Builder setWord(java.lang.CharSequence value) {
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
        public TfidfDocument.Builder clearWord() {
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
        public TfidfDocument.Builder setPostDate(java.lang.CharSequence value) {
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
        public TfidfDocument.Builder clearPostDate() {
            postDate = null;
            fieldSetFlags()[2] = false;
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
        public TfidfDocument.Builder setTfidfValue(java.lang.CharSequence value) {
            validate(fields()[3], value);
            this.tfidfValue = value;
            fieldSetFlags()[3] = true;
            return this;
        }

        /**
         * Checks whether the 'tfidfValue' field has been set
         */
        public boolean hasTfidfValue() {
            return fieldSetFlags()[3];
        }

        /**
         * Clears the value of the 'tfidfValue' field
         */
        public TfidfDocument.Builder clearTfidfValue() {
            tfidfValue = null;
            fieldSetFlags()[3] = false;
            return this;
        }

        @Override
        public TfidfDocument build() {
            try {
                TfidfDocument record = new TfidfDocument();
                record.title = fieldSetFlags()[0] ? this.title : (java.lang.CharSequence) defaultValue(fields()[0]);
                record.word = fieldSetFlags()[1] ? this.word : (java.lang.CharSequence) defaultValue(fields()[1]);
                record.postDate = fieldSetFlags()[2] ? this.postDate : (java.lang.CharSequence) defaultValue(fields()[2]);
                record.tfidfValue = fieldSetFlags()[3] ? this.tfidfValue : (java.lang.CharSequence) defaultValue(fields()[3]);
                return record;
            } catch (Exception e) {
                throw new org.apache.avro.AvroRuntimeException(e);
            }
        }
    }
}
