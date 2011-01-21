package dbnp.importer

import nl.grails.plugins.gdt.*

/**
* name: column name (in Excel)
* templatefieldtype: GSCF field type
* entity: GSCF entity class
* property: GSCF template field
* index: column index
* value: column value (optional, normally only name is used)
* identifier: true if this column is identifying (unique/primary key)
*/
class MappingColumn implements Serializable {

	String name
	nl.grails.plugins.gdt.TemplateFieldType templatefieldtype
	Class entity
	String property
	Integer index
	String value
	Boolean identifier
	Boolean dontimport

    static transients = [ "templatefieldtype", "entity" ]

    static constraints = {
	    name(unique: true)
	    dontimport(default:false)
    }

    static mapping = {
            tablePerHierarchy false
    }

    String toString() {
	return "Name:" + name + "/TemplateFieldType:" + templatefieldtype + "/Entity:" + entity + "/Property:" + property + "/Index:" + index + "/Value:" + value + "/Identifier:" + identifier
    }
}
