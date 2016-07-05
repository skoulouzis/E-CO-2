package eu.edisonproject.traning.utility.term.avro;

/**
 *
 * Copyright 2016 Michele Sparamonti & Spiros Koulouzis
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class Term extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {

    public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Term\",\"namespace\":\"term.avro\",\"fields\":[{\"name\":\"uid\",\"type\":\"string\"},{\"name\":\"lemma\",\"type\":\"string\"},{\"name\":\"glosses\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"originalTerm\",\"type\":\"string\"},{\"name\":\"url\",\"type\":\"string\"},{\"name\":\"categories\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"altLables\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"buids\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"nuids\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"confidence\",\"type\":\"double\"}]}");

    public static org.apache.avro.Schema getClassSchema() {
        return SCHEMA$;
    }
   
    public java.lang.CharSequence uid;
   
    public java.lang.CharSequence lemma;
   
    public java.util.List<java.lang.CharSequence> glosses;
   
    public java.lang.CharSequence originalTerm;
   
    public java.lang.CharSequence url;
   
    public java.util.List<java.lang.CharSequence> categories;
   
    public java.util.List<java.lang.CharSequence> altLables;
   
    public java.util.List<java.lang.CharSequence> buids;
   
    public java.util.List<java.lang.CharSequence> nuids;
 
    public double confidence;

    /**
     * Default constructor. Note that this does not initialize fields to their
     * default values from the schema. If that is desired then one should use
     * <code>newBuilder()</code>.
     */
    public Term() {
    }

    /**
     * All-args constructor.
     * @param uid
     * @param lemma
     * @param glosses
     * @param originalTerm
     * @param url
     * @param categories
     * @param altLables
     * @param buids
     * @param nuids
     * @param confidence
     */
    public Term(java.lang.CharSequence uid, java.lang.CharSequence lemma, java.util.List<java.lang.CharSequence> glosses, java.lang.CharSequence originalTerm, java.lang.CharSequence url, java.util.List<java.lang.CharSequence> categories, java.util.List<java.lang.CharSequence> altLables, java.util.List<java.lang.CharSequence> buids, java.util.List<java.lang.CharSequence> nuids, java.lang.Double confidence) {
        this.uid = uid;
        this.lemma = lemma;
        this.glosses = glosses;
        this.originalTerm = originalTerm;
        this.url = url;
        this.categories = categories;
        this.altLables = altLables;
        this.buids = buids;
        this.nuids = nuids;
        this.confidence = confidence;
    }

    @Override
    public org.apache.avro.Schema getSchema() {
        return SCHEMA$;
    }
    // Used by DatumWriter.  Applications should not call. 

    @Override
    public java.lang.Object get(int field$) {
        switch (field$) {
            case 0:
                return uid;
            case 1:
                return lemma;
            case 2:
                return glosses;
            case 3:
                return originalTerm;
            case 4:
                return url;
            case 5:
                return categories;
            case 6:
                return altLables;
            case 7:
                return buids;
            case 8:
                return nuids;
            case 9:
                return confidence;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }
    // Used by DatumReader.  Applications should not call. 

    @SuppressWarnings(value = "unchecked")
    @Override
    public void put(int field$, java.lang.Object value$) {
        switch (field$) {
            case 0:
                uid = (java.lang.CharSequence) value$;
                break;
            case 1:
                lemma = (java.lang.CharSequence) value$;
                break;
            case 2:
                glosses = (java.util.List<java.lang.CharSequence>) value$;
                break;
            case 3:
                originalTerm = (java.lang.CharSequence) value$;
                break;
            case 4:
                url = (java.lang.CharSequence) value$;
                break;
            case 5:
                categories = (java.util.List<java.lang.CharSequence>) value$;
                break;
            case 6:
                altLables = (java.util.List<java.lang.CharSequence>) value$;
                break;
            case 7:
                buids = (java.util.List<java.lang.CharSequence>) value$;
                break;
            case 8:
                nuids = (java.util.List<java.lang.CharSequence>) value$;
                break;
            case 9:
                confidence = (java.lang.Double) value$;
                break;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    /**
     * Gets the value of the 'uid' field.
     * @return 
     */
    public java.lang.CharSequence getUid() {
        return uid;
    }

    /**
     * Sets the value of the 'uid' field.
     *
     * @param value the value to set.
     */
    public void setUid(java.lang.CharSequence value) {
        this.uid = value;
    }

    /**
     * Gets the value of the 'lemma' field.
     * @return 
     */
    public java.lang.CharSequence getLemma() {
        return lemma;
    }

    /**
     * Sets the value of the 'lemma' field.
     *
     * @param value the value to set.
     */
    public void setLemma(java.lang.CharSequence value) {
        this.lemma = value;
    }

    /**
     * Gets the value of the 'glosses' field.
     * @return 
     */
    public java.util.List<java.lang.CharSequence> getGlosses() {
        return glosses;
    }

    /**
     * Sets the value of the 'glosses' field.
     *
     * @param value the value to set.
     */
    public void setGlosses(java.util.List<java.lang.CharSequence> value) {
        this.glosses = value;
    }

    /**
     * Gets the value of the 'originalTerm' field.
     */
    public java.lang.CharSequence getOriginalTerm() {
        return originalTerm;
    }

    /**
     * Sets the value of the 'originalTerm' field.
     *
     * @param value the value to set.
     */
    public void setOriginalTerm(java.lang.CharSequence value) {
        this.originalTerm = value;
    }

    /**
     * Gets the value of the 'url' field.
     */
    public java.lang.CharSequence getUrl() {
        return url;
    }

    /**
     * Sets the value of the 'url' field.
     *
     * @param value the value to set.
     */
    public void setUrl(java.lang.CharSequence value) {
        this.url = value;
    }

    /**
     * Gets the value of the 'categories' field.
     */
    public java.util.List<java.lang.CharSequence> getCategories() {
        return categories;
    }

    /**
     * Sets the value of the 'categories' field.
     *
     * @param value the value to set.
     */
    public void setCategories(java.util.List<java.lang.CharSequence> value) {
        this.categories = value;
    }

    /**
     * Gets the value of the 'altLables' field.
     */
    public java.util.List<java.lang.CharSequence> getAltLables() {
        return altLables;
    }

    /**
     * Sets the value of the 'altLables' field.
     *
     * @param value the value to set.
     */
    public void setAltLables(java.util.List<java.lang.CharSequence> value) {
        this.altLables = value;
    }

    /**
     * Gets the value of the 'buids' field.
     */
    public java.util.List<java.lang.CharSequence> getBuids() {
        return buids;
    }

    /**
     * Sets the value of the 'buids' field.
     *
     * @param value the value to set.
     */
    public void setBuids(java.util.List<java.lang.CharSequence> value) {
        this.buids = value;
    }

    /**
     * Gets the value of the 'nuids' field.
     */
    public java.util.List<java.lang.CharSequence> getNuids() {
        return nuids;
    }

    /**
     * Sets the value of the 'nuids' field.
     *
     * @param value the value to set.
     */
    public void setNuids(java.util.List<java.lang.CharSequence> value) {
        this.nuids = value;
    }

    /**
     * Gets the value of the 'confidence' field.
     */
    public java.lang.Double getConfidence() {
        return confidence;
    }

    /**
     * Sets the value of the 'confidence' field.
     *
     * @param value the value to set.
     */
    public void setConfidence(java.lang.Double value) {
        this.confidence = value;
    }

    /**
     * Creates a new Term RecordBuilder
     */
    public static Term.Builder newBuilder() {
        return new Term.Builder();
    }

    /**
     * Creates a new Term RecordBuilder by copying an existing Builder
     */
    public static Term.Builder newBuilder(Term.Builder other) {
        return new Term.Builder(other);
    }

    /**
     * Creates a new Term RecordBuilder by copying an existing Term instance
     */
    public static Term.Builder newBuilder(Term other) {
        return new Term.Builder(other);
    }

    /**
     * RecordBuilder for Term instances.
     */
    public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<Term>
            implements org.apache.avro.data.RecordBuilder<Term> {

        private java.lang.CharSequence uid;
        private java.lang.CharSequence lemma;
        private java.util.List<java.lang.CharSequence> glosses;
        private java.lang.CharSequence originalTerm;
        private java.lang.CharSequence url;
        private java.util.List<java.lang.CharSequence> categories;
        private java.util.List<java.lang.CharSequence> altLables;
        private java.util.List<java.lang.CharSequence> buids;
        private java.util.List<java.lang.CharSequence> nuids;
        private double confidence;

        /**
         * Creates a new Builder
         */
        private Builder() {
            super(Term.SCHEMA$);
        }

        /**
         * Creates a Builder by copying an existing Builder
         */
        private Builder(Term.Builder other) {
            super(other);
            if (isValidValue(fields()[0], other.uid)) {
                this.uid = data().deepCopy(fields()[0].schema(), other.uid);
                fieldSetFlags()[0] = true;
            }
            if (isValidValue(fields()[1], other.lemma)) {
                this.lemma = data().deepCopy(fields()[1].schema(), other.lemma);
                fieldSetFlags()[1] = true;
            }
            if (isValidValue(fields()[2], other.glosses)) {
                this.glosses = data().deepCopy(fields()[2].schema(), other.glosses);
                fieldSetFlags()[2] = true;
            }
            if (isValidValue(fields()[3], other.originalTerm)) {
                this.originalTerm = data().deepCopy(fields()[3].schema(), other.originalTerm);
                fieldSetFlags()[3] = true;
            }
            if (isValidValue(fields()[4], other.url)) {
                this.url = data().deepCopy(fields()[4].schema(), other.url);
                fieldSetFlags()[4] = true;
            }
            if (isValidValue(fields()[5], other.categories)) {
                this.categories = data().deepCopy(fields()[5].schema(), other.categories);
                fieldSetFlags()[5] = true;
            }
            if (isValidValue(fields()[6], other.altLables)) {
                this.altLables = data().deepCopy(fields()[6].schema(), other.altLables);
                fieldSetFlags()[6] = true;
            }
            if (isValidValue(fields()[7], other.buids)) {
                this.buids = data().deepCopy(fields()[7].schema(), other.buids);
                fieldSetFlags()[7] = true;
            }
            if (isValidValue(fields()[8], other.nuids)) {
                this.nuids = data().deepCopy(fields()[8].schema(), other.nuids);
                fieldSetFlags()[8] = true;
            }
            if (isValidValue(fields()[9], other.confidence)) {
                this.confidence = data().deepCopy(fields()[9].schema(), other.confidence);
                fieldSetFlags()[9] = true;
            }
        }

        /**
         * Creates a Builder by copying an existing Term instance
         */
        private Builder(Term other) {
            super(Term.SCHEMA$);
            if (isValidValue(fields()[0], other.uid)) {
                this.uid = data().deepCopy(fields()[0].schema(), other.uid);
                fieldSetFlags()[0] = true;
            }
            if (isValidValue(fields()[1], other.lemma)) {
                this.lemma = data().deepCopy(fields()[1].schema(), other.lemma);
                fieldSetFlags()[1] = true;
            }
            if (isValidValue(fields()[2], other.glosses)) {
                this.glosses = data().deepCopy(fields()[2].schema(), other.glosses);
                fieldSetFlags()[2] = true;
            }
            if (isValidValue(fields()[3], other.originalTerm)) {
                this.originalTerm = data().deepCopy(fields()[3].schema(), other.originalTerm);
                fieldSetFlags()[3] = true;
            }
            if (isValidValue(fields()[4], other.url)) {
                this.url = data().deepCopy(fields()[4].schema(), other.url);
                fieldSetFlags()[4] = true;
            }
            if (isValidValue(fields()[5], other.categories)) {
                this.categories = data().deepCopy(fields()[5].schema(), other.categories);
                fieldSetFlags()[5] = true;
            }
            if (isValidValue(fields()[6], other.altLables)) {
                this.altLables = data().deepCopy(fields()[6].schema(), other.altLables);
                fieldSetFlags()[6] = true;
            }
            if (isValidValue(fields()[7], other.buids)) {
                this.buids = data().deepCopy(fields()[7].schema(), other.buids);
                fieldSetFlags()[7] = true;
            }
            if (isValidValue(fields()[8], other.nuids)) {
                this.nuids = data().deepCopy(fields()[8].schema(), other.nuids);
                fieldSetFlags()[8] = true;
            }
            if (isValidValue(fields()[9], other.confidence)) {
                this.confidence = data().deepCopy(fields()[9].schema(), other.confidence);
                fieldSetFlags()[9] = true;
            }
        }

        /**
         * Gets the value of the 'uid' field
         */
        public java.lang.CharSequence getUid() {
            return uid;
        }

        /**
         * Sets the value of the 'uid' field
         * @param value
         * @return 
         */
        public Term.Builder setUid(java.lang.CharSequence value) {
            validate(fields()[0], value);
            this.uid = value;
            fieldSetFlags()[0] = true;
            return this;
        }

        /**
         * Checks whether the 'uid' field has been set
         * @return 
         */
        public boolean hasUid() {
            return fieldSetFlags()[0];
        }

        /**
         * Clears the value of the 'uid' field
         * @return 
         */
        public Term.Builder clearUid() {
            uid = null;
            fieldSetFlags()[0] = false;
            return this;
        }

        /**
         * Gets the value of the 'lemma' field
         * @return 
         */
        public java.lang.CharSequence getLemma() {
            return lemma;
        }

        /**
         * Sets the value of the 'lemma' field
         * @param value
         * @return 
         */
        public Term.Builder setLemma(java.lang.CharSequence value) {
            validate(fields()[1], value);
            this.lemma = value;
            fieldSetFlags()[1] = true;
            return this;
        }

        /**
         * Checks whether the 'lemma' field has been set
         * @return 
         */
        public boolean hasLemma() {
            return fieldSetFlags()[1];
        }

        /**
         * Clears the value of the 'lemma' field
         * @return 
         */
        public Term.Builder clearLemma() {
            lemma = null;
            fieldSetFlags()[1] = false;
            return this;
        }

        /**
         * Gets the value of the 'glosses' field
         * @return 
         */
        public java.util.List<java.lang.CharSequence> getGlosses() {
            return glosses;
        }

        /**
         * Sets the value of the 'glosses' field
         */
        public Term.Builder setGlosses(java.util.List<java.lang.CharSequence> value) {
            validate(fields()[2], value);
            this.glosses = value;
            fieldSetFlags()[2] = true;
            return this;
        }

        /**
         * Checks whether the 'glosses' field has been set
         */
        public boolean hasGlosses() {
            return fieldSetFlags()[2];
        }

        /**
         * Clears the value of the 'glosses' field
         */
        public Term.Builder clearGlosses() {
            glosses = null;
            fieldSetFlags()[2] = false;
            return this;
        }

        /**
         * Gets the value of the 'originalTerm' field
         */
        public java.lang.CharSequence getOriginalTerm() {
            return originalTerm;
        }

        /**
         * Sets the value of the 'originalTerm' field
         */
        public Term.Builder setOriginalTerm(java.lang.CharSequence value) {
            validate(fields()[3], value);
            this.originalTerm = value;
            fieldSetFlags()[3] = true;
            return this;
        }

        /**
         * Checks whether the 'originalTerm' field has been set
         */
        public boolean hasOriginalTerm() {
            return fieldSetFlags()[3];
        }

        /**
         * Clears the value of the 'originalTerm' field
         */
        public Term.Builder clearOriginalTerm() {
            originalTerm = null;
            fieldSetFlags()[3] = false;
            return this;
        }

        /**
         * Gets the value of the 'url' field
         */
        public java.lang.CharSequence getUrl() {
            return url;
        }

        /**
         * Sets the value of the 'url' field
         */
        public Term.Builder setUrl(java.lang.CharSequence value) {
            validate(fields()[4], value);
            this.url = value;
            fieldSetFlags()[4] = true;
            return this;
        }

        /**
         * Checks whether the 'url' field has been set
         */
        public boolean hasUrl() {
            return fieldSetFlags()[4];
        }

        /**
         * Clears the value of the 'url' field
         */
        public Term.Builder clearUrl() {
            url = null;
            fieldSetFlags()[4] = false;
            return this;
        }

        /**
         * Gets the value of the 'categories' field
         */
        public java.util.List<java.lang.CharSequence> getCategories() {
            return categories;
        }

        /**
         * Sets the value of the 'categories' field
         */
        public Term.Builder setCategories(java.util.List<java.lang.CharSequence> value) {
            validate(fields()[5], value);
            this.categories = value;
            fieldSetFlags()[5] = true;
            return this;
        }

        /**
         * Checks whether the 'categories' field has been set
         */
        public boolean hasCategories() {
            return fieldSetFlags()[5];
        }

        /**
         * Clears the value of the 'categories' field
         */
        public Term.Builder clearCategories() {
            categories = null;
            fieldSetFlags()[5] = false;
            return this;
        }

        /**
         * Gets the value of the 'altLables' field
         */
        public java.util.List<java.lang.CharSequence> getAltLables() {
            return altLables;
        }

        /**
         * Sets the value of the 'altLables' field
         */
        public Term.Builder setAltLables(java.util.List<java.lang.CharSequence> value) {
            validate(fields()[6], value);
            this.altLables = value;
            fieldSetFlags()[6] = true;
            return this;
        }

        /**
         * Checks whether the 'altLables' field has been set
         */
        public boolean hasAltLables() {
            return fieldSetFlags()[6];
        }

        /**
         * Clears the value of the 'altLables' field
         */
        public Term.Builder clearAltLables() {
            altLables = null;
            fieldSetFlags()[6] = false;
            return this;
        }

        /**
         * Gets the value of the 'buids' field
         */
        public java.util.List<java.lang.CharSequence> getBuids() {
            return buids;
        }

        /**
         * Sets the value of the 'buids' field
         */
        public Term.Builder setBuids(java.util.List<java.lang.CharSequence> value) {
            validate(fields()[7], value);
            this.buids = value;
            fieldSetFlags()[7] = true;
            return this;
        }

        /**
         * Checks whether the 'buids' field has been set
         */
        public boolean hasBuids() {
            return fieldSetFlags()[7];
        }

        /**
         * Clears the value of the 'buids' field
         */
        public Term.Builder clearBuids() {
            buids = null;
            fieldSetFlags()[7] = false;
            return this;
        }

        /**
         * Gets the value of the 'nuids' field
         */
        public java.util.List<java.lang.CharSequence> getNuids() {
            return nuids;
        }

        /**
         * Set the value of the 'nuids' field
         */
        public Term.Builder setNuids(java.util.List<java.lang.CharSequence> value) {
            validate(fields()[8], value);
            this.nuids = value;
            fieldSetFlags()[8] = true;
            return this;
        }

        /**
         * Checks whether the 'nuids' field has been set
         */
        public boolean hasNuids() {
            return fieldSetFlags()[8];
        }

        /**
         * Clears the value of the 'nuids' field
         */
        public Term.Builder clearNuids() {
            nuids = null;
            fieldSetFlags()[8] = false;
            return this;
        }

        /**
         * Gets the value of the 'confidence' field
         */
        public java.lang.Double getConfidence() {
            return confidence;
        }

        /**
         * Sets the value of the 'confidence' field
         */
        public Term.Builder setConfidence(double value) {
            validate(fields()[9], value);
            this.confidence = value;
            fieldSetFlags()[9] = true;
            return this;
        }

        /**
         * Checks whether the 'confidence' field has been set
         */
        public boolean hasConfidence() {
            return fieldSetFlags()[9];
        }

        /**
         * Clears the value of the 'confidence' field
         */
        public Term.Builder clearConfidence() {
            fieldSetFlags()[9] = false;
            return this;
        }

        @Override
        public Term build() {
            try {
                Term record = new Term();
                record.uid = fieldSetFlags()[0] ? this.uid : (java.lang.CharSequence) defaultValue(fields()[0]);
                record.lemma = fieldSetFlags()[1] ? this.lemma : (java.lang.CharSequence) defaultValue(fields()[1]);
                record.glosses = fieldSetFlags()[2] ? this.glosses : (java.util.List<java.lang.CharSequence>) defaultValue(fields()[2]);
                record.originalTerm = fieldSetFlags()[3] ? this.originalTerm : (java.lang.CharSequence) defaultValue(fields()[3]);
                record.url = fieldSetFlags()[4] ? this.url : (java.lang.CharSequence) defaultValue(fields()[4]);
                record.categories = fieldSetFlags()[5] ? this.categories : (java.util.List<java.lang.CharSequence>) defaultValue(fields()[5]);
                record.altLables = fieldSetFlags()[6] ? this.altLables : (java.util.List<java.lang.CharSequence>) defaultValue(fields()[6]);
                record.buids = fieldSetFlags()[7] ? this.buids : (java.util.List<java.lang.CharSequence>) defaultValue(fields()[7]);
                record.nuids = fieldSetFlags()[8] ? this.nuids : (java.util.List<java.lang.CharSequence>) defaultValue(fields()[8]);
                record.confidence = fieldSetFlags()[9] ? this.confidence : (java.lang.Double) defaultValue(fields()[9]);
                return record;
            } catch (Exception e) {
                throw new org.apache.avro.AvroRuntimeException(e);
            }
        }
    }
}
