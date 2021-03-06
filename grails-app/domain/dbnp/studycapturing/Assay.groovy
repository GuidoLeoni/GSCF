package dbnp.studycapturing

import org.dbnp.gdt.*

/**
 * This class describes an Assay, which describes the application of a certain (omics) measurement to multiple samples.
 * The actual data of these measurements are described in submodules of dbNP. The type property describes in which module
 * this data can be found.
 */
class Assay extends TemplateEntity {
    // The name of the assay, which should indicate the measurements represented in this assay to the user.
    String name

    // The dbNP module in which the assay omics data can be found. */
    AssayModule module

    /**
     * UUID of this assay
     */
    String assayUUID

    /**
     * return the domain fields for this domain class
     * @return List
     */
    static List<TemplateField> giveDomainFields() { return Assay.domainFields }

    static List<TemplateField> domainFields = [
            new TemplateField(
                    name: 'name',
                    type: TemplateFieldType.STRING,
                    preferredIdentifier: true,
                    comment: 'The name you give here is used to discern this assay within the study (e.g. \'liver transcriptomics\', \'blood lipidomics\')',
                    required: true
            ),
            new TemplateField(
                    name: 'module',
                    type: TemplateFieldType.MODULE,
                    comment: 'Select the dbNP module where the actual assay measurement data is stored',
                    required: true
            )
    ]

    // An Assay always belongs to one study.
    static belongsTo = [parent: Study]

    // An Assay can have many samples on which it is performed, but all samples should be within the 'parent' Study.
    static hasMany = [samples: Sample]

    static constraints = {
        assayUUID(nullable: true, unique: true)
    }

    static mapping = {
	    cache true
        sort "name"

        // Workaround for bug http://jira.codehaus.org/browse/GRAILS-6754
        templateTextFields type: 'text'
    }

    def String toString() {
        return name;
    }

    def getToken() {
        return giveUUID()
    }

    /**
     * Basic equals method to check whether objects are equals, by comparing the ids
     * @param o Object to compare with
     * @return True iff the id of the given Study is equal to the id of this Study
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;

        if (!(o instanceof Assay))
            return false

        Assay s = (Assay) o;

        return this.id == s.id
    }

    /**
     * Returns the UUID of this sample and generates one if needed
     */
    public String giveUUID() {
        if (!this.assayUUID) {
            this.assayUUID = UUID.randomUUID().toString();
            if (!this.save(flush: true)) {
                log.error "Couldn't save assay UUID: " + this.getErrors();
            }
        }

        return this.assayUUID;
    }
}
