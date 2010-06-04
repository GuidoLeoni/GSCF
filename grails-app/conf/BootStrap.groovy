import dbnp.studycapturing.*

import dbnp.data.Ontology
import dbnp.data.Term
import org.codehaus.groovy.grails.commons.GrailsApplication

/**
 * Application Bootstrapper
 * @Author Jeroen Wesbeek
 * @Since 20091021
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
class BootStrap {
	def init = {servletContext ->
		// define timezone
		System.setProperty('user.timezone', 'CET')

		// we could also check if we are in development by GrailsUtil.environment == GrailsApplication.ENV_DEVELOPMENT
		if (Study.count() == 0) {
			println ".development bootstrapping...";

			// add Subject>species ontology
			println ".adding NCBI species ontology"
			def speciesOntology = new Ontology(
				name: 'NCBI organismal classification',
				description: 'A taxonomic classification of living organisms and associated artifacts for their controlled description within the context of databases.',
				url: 'http://www.ncbi.nlm.nih.gov/Taxonomy/taxonomyhome.html/',
				versionNumber: '1.2',
				ncboId: '1132',
				ncboVersionedId: '38802'
			).with { if (!validate()) { errors.each { println it} } else save()}

			// add Sample>material ontology
			println ".adding BRENDA source material ontology"
			def brendaOntology = new Ontology(
				name: 'BRENDA tissue / enzyme source',
				description: 'A structured controlled vocabulary for the source of an enzyme. It comprises terms for tissues, cell lines, cell types and cell cultures from uni- and multicellular organisms.',
				url: 'http://www.brenda-enzymes.info',
				versionNumber: '1.3',
				ncboId: '1005',
				ncboVersionedId: '40643'
			).with { if (!validate()) { errors.each { println it} } else save()}

			// add NCI ontology which is used in Mouse genotype template field
			def nciOntology = new Ontology(
				name: 'NCI Thesaurus',
				description: 'A vocabulary for clinical care, translational and basic research, and public information and administrative activities.',
				url: 'http://ncicb.nci.nih.gov/core/EVS',
				versionNumber: '10.01',
				ncboId: '1032',
				ncboVersionedId: '42693'
			).with { if (!validate()) { errors.each { println it} } else save()}
			
			// add Terms
			println ".adding mouse term"
			def mouseTerm = new Term(
				name: 'Mus musculus',
				ontology: speciesOntology,
				accession: '10090'
			).with { if (!validate()) { errors.each { println it} } else save()}
			println ".adding human term"
			def humanTerm = new Term(
				name: 'Homo sapiens',
				ontology: speciesOntology,
				accession: '9606'
			).with { if (!validate()) { errors.each { println it} } else save()}
			def arabTerm = new Term(
				name: 'Arabidopsis thaliana',
				ontology: speciesOntology,
				accession: '3702'
			).with { if (!validate()) { errors.each { println it} } else save()}

			def bloodTerm = new Term(
				name: 'blood plasma',
				ontology: brendaOntology,
				accession: 'BTO:0000131'
			).with { if (!validate()) { errors.each { println it} } else save()}

			def c57bl6Term = new Term(
				name: 'C57BL/6 Mouse',
				ontology: nciOntology,
				accession: 'C14424'
			).with { if (!validate()) { errors.each { println it} } else save()}

			// Create a few persons, roles and Affiliations
			println ".adding persons, roles and affiliations"
			def affiliation1 = new PersonAffiliation(
			    institute: "Science Institute NYC",
                            department: "Department of Mathematics"
			).save();
			def affiliation2 = new PersonAffiliation(
			    institute: "InfoStats GmbH, Hamburg",
                            department: "Life Sciences"
			).save();
			def role1 = new PersonRole(
			    name: "Principal Investigator"
			).save();
			def role2 = new PersonRole(
			    name: "Statician"
			).save();

			// Create persons
			def person1 = new Person(
			    lastName: "Scientist",
			    firstName: "John",
			    gender: "Male",
			    initials: "J.R.",
			    email: "john@scienceinstitute.com",
			    phone: "1-555-3049",
			    address: "First street 2,NYC"
			)
			.addToAffiliations( affiliation1 )
			.addToAffiliations( affiliation2 )
			.save();

			def person2 = new Person(
			    lastName: "Statician",
			    firstName: "Jane",
			    gender: "Female",
			    initials: "W.J.",
			    email: "jane@statisticalcompany.de",
			    phone: "49-555-8291",
			    address: "Dritten strasse 38, Hamburg, Germany"
			)
			.addToAffiliations( affiliation2 )
			.save();

			// Create 30 persons to test pagination
			def personCounter = 1;
			30.times { new Person( firstName: "Person #${personCounter}", lastName: "Testperson", email: "email${personCounter++}@testdomain.com" ).save() }

			// Create a few publications
			println ".adding publications"
			def publication1 = new Publication(
                            title: "Postnatal development of hypothalamic leptin receptors",
			    authorsList: "Cottrell EC, Mercer JG, Ozanne SE.",
			    pubMedID: "20472140",
			    comments: "Not published yet",
			    DOI: "unknown"
			)
			.save();

			def publication2 = new Publication(
                            title: "Induction of regulatory T cells decreases adipose inflammation and alleviates insulin resistance in ob/ob mice",
			    authorsList: "Ilan Y, Maron R, Tukpah AM, Maioli TU, Murugaiyan G, Yang K, Wu HY, Weiner HL.",
			    pubMedID: "20445103",
			    comments: "",
			    DOI: ""
			)
			.save();

            // Create templates

			def genderField = new TemplateField(
				name: 'Gender',type: TemplateFieldType.STRINGLIST,
				listEntries: [new TemplateFieldListItem(name:'Male'),new TemplateFieldListItem(name: 'Female'),new TemplateFieldListItem(name: 'Unknown')])
			.with { if (!validate()) { errors.each { println it} } else save()}

			def ageField = new TemplateField(
				name: 'Age',type: TemplateFieldType.INTEGER,unit: 'years',comment: 'Either include age at the start of the study or date of birth (if known)')
			.with { if (!validate()) { errors.each { println it} } else save()}

			def genotypeField = new TemplateField(
				name: 'Genotype', type: TemplateFieldType.STRING,
				comment: 'If present, indicate the genetic variance of the subject (the gene knock-out/in or transgene)')
			.with { if (!validate()) { errors.each { println it} } else save()}

			def genotypeTypeField = new TemplateField(
				name: 'Genotype type',type: TemplateFieldType.STRINGLIST,
				listEntries: [new TemplateFieldListItem(name:'wildtype'),
					new TemplateFieldListItem(name:'transgenic'),
					new TemplateFieldListItem(name:'knock-out'),
					new TemplateFieldListItem(name:'knock-in')],
				comment: 'If a genotype was specified, please indicate here the type of the genotype')	
			.with { if (!validate()) { errors.each { println it} } else save()}


			// Nutritional study template

			println ".adding academic study template..."
			def studyTemplate = new Template(
				name: 'Academic study', entity: dbnp.studycapturing.Study)
				.addToFields(new TemplateField(name: 'Description',type: TemplateFieldType.TEXT,comment:'Describe here the type of subjects and the treatment, challenges and sampling.'))
				.addToFields(new TemplateField(
					name: 'Study code',
					type: TemplateFieldType.STRING,
					preferredIdentifier:true,
					comment: 'Fill out the code by which many people will recognize your study'))
				.addToFields(new TemplateField(name: 'Objectives',type: TemplateFieldType.TEXT,comment:'Fill out the aim or questions of the study'))
				.addToFields(new TemplateField(name: 'Consortium',type: TemplateFieldType.STRING,comment:'If the study was performed within a consortium (e.g. NMC, NuGO), you can indicate this here'))
				.addToFields(new TemplateField(name: 'Cohort name',type: TemplateFieldType.STRING,comment:'If a cohort was used the name or code of the cohort can be define here (define a cohort template)'))
				//.addToFields(new TemplateField(name: 'Time zone',type: TemplateFieldType.STRING,comment:'In the database the local time will be stored. This field is essential to be able to generalize time.'))
				.addToFields(new TemplateField(name: 'Responsible scientist',type: TemplateFieldType.STRING,comment:'Fill out the project leader of principle investigator of the study. (soon to be replaced with persons input)'))
				.addToFields(new TemplateField(name: 'Lab id',type: TemplateFieldType.STRING,comment:'In which lab was the study performed; indicate the roomnumber.'))
				.addToFields(new TemplateField(name: 'Institute',type: TemplateFieldType.STRING,comment:'In which institute was the study performed; indicate the full address information (to be replaced by persons-affiliations?)'))
                                //commented out because of bug #84:.addToFields(new TemplateField(name: 'Study protocol',type: TemplateFieldType.FILE,comment:'Optionally attach a file in which the protocol in the study is described'))
			.with { if (!validate()) { errors.each { println it} } else save()}

			// Mouse template
			println ".adding mouse subject template..."
			def mouseTemplate = new Template(
				name: 'Mouse', entity: dbnp.studycapturing.Subject)
			.addToFields(new TemplateField(
				name: 'Strain', type: TemplateFieldType.ONTOLOGYTERM, ontologies: [nciOntology], comment: "This is an ontology term, if the right strain is not in the list please add it with 'add more'"))
			.addToFields(genotypeField)
			.addToFields(genotypeTypeField)
			.addToFields(genderField)
			.addToFields(new TemplateField(
				name: 'Age', type: TemplateFieldType.INTEGER, unit: 'weeks', comment: 'Age at start of study'))
			.addToFields(new TemplateField(
				name: 'Age type',type: TemplateFieldType.STRINGLIST,
				listEntries: [new TemplateFieldListItem(name:'postnatal'),new TemplateFieldListItem(name:'embryonal')]))
			.addToFields(new TemplateField(
				name: 'Cage',type: TemplateFieldType.STRING,comment:'Indicate the cage used for housing (type and/or size)'))
			.addToFields(new TemplateField(
				name: '#Mice in cage',type: TemplateFieldType.INTEGER,comment:'If known, indicate the number of mice per cage'))
			.addToFields(new TemplateField(
				name: 'Litter size',type: TemplateFieldType.INTEGER,comment:'If known, indicate the litter size of the litter from which the subject originates'))
			.addToFields(new TemplateField(
				name: 'Weight', type: TemplateFieldType.DOUBLE, unit: 'gram',comment:'If known indicate the weight of the subject in grams at the start of the study'))
			.with { if (!validate()) { errors.each { println it} } else save()}

			// Human template
			println ".adding human subject template..."
			def humanTemplate = new Template(
				name: 'Human', entity: dbnp.studycapturing.Subject)
			.addToFields(genderField)
			.addToFields(ageField)
			.addToFields(new TemplateField(
				name: 'DOB',type: TemplateFieldType.DATE,comment:'Date of birth'))
			.addToFields(new TemplateField(
				name: 'Height',type: TemplateFieldType.DOUBLE, unit: 'm'))
			.addToFields(new TemplateField(
				name: 'Weight',type: TemplateFieldType.DOUBLE, unit: 'kg'))
			.addToFields(new TemplateField(
				name: 'BMI',type: TemplateFieldType.DOUBLE, unit: 'kg/m2',comment:'Body-mass-index'))
			.addToFields(new TemplateField(
				name: 'Race',type: TemplateFieldType.STRING,comment:'If known and of interest the ethnic group can be indicated'))
			.addToFields(new TemplateField(
				name: 'Waist circumference',type: TemplateFieldType.FLOAT, unit: 'cm',comment:'The waist circumference is measured just above the hip bone. Indicate the measure at the start of the study.'))
			.addToFields(new TemplateField(
				name: 'Hip circumference',type: TemplateFieldType.FLOAT, unit: 'cm',comment:'The hip circumference is measured at the level of the two bony prominences front of the hips. Indicate the measure at the start of the study.'))
			.addToFields(new TemplateField(
				name: 'Systolic blood pressure',type: TemplateFieldType.FLOAT, unit: 'mmHg',comment:'Indicate the levels at the start of the study in mmHG'))
			.addToFields(new TemplateField(
				name: 'Diastolic blood pressure',type: TemplateFieldType.FLOAT, unit: 'mmHg',comment:'Indicate the levels at the start of the study in mmHG'))
			.addToFields(new TemplateField(
				name: 'Heart rate',type: TemplateFieldType.FLOAT, unit: 'beats/min',comment:'Indicate the heart rate at the start of in study in beats per minute'))
			.addToFields(new TemplateField(
				name: 'Run-in-food',type: TemplateFieldType.TEXT,comment:'If defined, give a short description of the food used before the measurements'))
			.with { if (!validate()) { errors.each { println it} } else save()}


			def sampleRemarksField = new TemplateField(
				name: 'Remarks',type: TemplateFieldType.TEXT)
			.with { if (!validate()) { errors.each { println it} } else save()}
			def sampleVialTextField = new TemplateField(
				name: 'Text on vial',type: TemplateFieldType.STRING)
			.with { if (!validate()) { errors.each { println it} } else save()}

			// Human tissue sample template
			println ".adding human sample template..."
			def humanSampleTemplate = new Template(
				name: 'Human tissue sample', entity: dbnp.studycapturing.Sample)
			.addToFields(sampleRemarksField)
			.addToFields(sampleVialTextField)
            .addToFields(new TemplateField(
                name: 'Sample measured weight',
                unit: 'mg',
                type: TemplateFieldType.FLOAT))
			.with { if (!validate()) { errors.each { println it} } else save()}

            // Human blood sample template
            println ".adding human sample template..."
            def humanBloodSampleTemplate = new Template(
                name: 'Human blood sample', entity: dbnp.studycapturing.Sample)
            .addToFields(sampleRemarksField)
            .addToFields(sampleVialTextField)
            .addToFields(new TemplateField(
                name: 'Sample measured volume',
                unit: 'ml',
                type: TemplateFieldType.FLOAT))
            .with { if (!validate()) { errors.each { println it} } else save()}

			//Plant template
			println ".adding plant template..."
			def plantTemplate = new Template(
				name: 'Plant template', entity: dbnp.studycapturing.Subject)
			.addToFields(new TemplateField(
				name: 'Variety', type: TemplateFieldType.STRING))
			.addToFields(new TemplateField(
				name: 'Ecotype', type: TemplateFieldType.STRING))
			.addToFields(genotypeField)
			.addToFields(genotypeTypeField)
			.addToFields(new TemplateField(
				name: 'Growth location', type: TemplateFieldType.STRINGLIST,
				listEntries: [new TemplateFieldListItem(name:'Greenhouse'),new TemplateFieldListItem(name: 'Field')]))
			.addToFields(new TemplateField(
				name: 'Room', type: TemplateFieldType.STRING,
				comment: 'Chamber number in case of Greenhouse'))
			.addToFields(new TemplateField(
				name: 'Position X', type: TemplateFieldType.FLOAT))
			.addToFields(new TemplateField(
				name: 'Position Y', type: TemplateFieldType.FLOAT))
			.addToFields(new TemplateField(
				name: 'Block', type: TemplateFieldType.STRING))
			.addToFields(new TemplateField(
				name: 'Temperature at day', type: TemplateFieldType.FLOAT))
			.addToFields(new TemplateField(
				name: 'Temperature at night', type: TemplateFieldType.FLOAT))
			.addToFields(new TemplateField(
				name: 'Photo period', type: TemplateFieldType.STRING))
			.addToFields(new TemplateField(
				name: 'Light intensity', type: TemplateFieldType.STRING))
			.addToFields(new TemplateField(
				name: 'Start date', type: TemplateFieldType.DATE))
			.addToFields(new TemplateField(
				name: 'Harvest date', type: TemplateFieldType.DATE))
			.addToFields(new TemplateField(
				name: 'Growth type', type: TemplateFieldType.STRINGLIST,
				listEntries: [new TemplateFieldListItem(name:'Standard'),new TemplateFieldListItem(name: 'Experimental')]))
			.addToFields(new TemplateField(
				name: 'Growth protocol', type: TemplateFieldType.TEXT))
			.addToFields(new TemplateField(
				name: 'Harvest delay', type: TemplateFieldType.TEXT))
			.with { if (!validate()) { errors.each { println it} } else save()}

			println ".adding plant sample template..."
			def plantSampleTemplate = new Template(
				name: 'Plant sample', entity: dbnp.studycapturing.Sample)
			.addToFields(sampleRemarksField)
			.addToFields(sampleVialTextField)
			.with { if (!validate()) { errors.each { println it} } else save()}

			def dietTreatmentTemplate = new Template(
				name: 'Diet treatment HF45/LF10', entity: dbnp.studycapturing.Event)
			.addToFields(new TemplateField(
				name: 'Diet', type: TemplateFieldType.STRINGLIST,
				listEntries: [new TemplateFieldListItem(name:'10% fat (palm oil)'),new TemplateFieldListItem(name: '45% fat (palm oil)')]))
			.with { if (!validate()) { errors.each { println it} } else save()}

			def boostTreatmentTemplate = new Template(
				name: 'Leptin treatment', entity: dbnp.studycapturing.Event)
			.addToFields(new TemplateField(
				name: 'Compound', type: TemplateFieldType.STRINGLIST,
				listEntries: [new TemplateFieldListItem(name:'Vehicle'),new TemplateFieldListItem(name: 'Leptin')]))
			.with { if (!validate()) { errors.each { println it} } else save()}

			def fastingTreatment = new Template(
                                name: 'Fasting treatment',
                                description: 'Fasting Protocol NuGO PPSH',
                                entity: dbnp.studycapturing.Event)
                        .addToFields(new TemplateField(
                                name: 'Fasting period',
                                type: TemplateFieldType.STRING))
			 .with { if (!validate()) { errors.each { println it} } else save()}

			// SamplingEvent templates

                        def samplingProtocolField = new TemplateField(
                        name: 'Sample Protocol',type: TemplateFieldType.STRING)
                        .with { if (!validate()) { errors.each { println it} } else save()}

			def liverSamplingEventTemplate = new Template(
				name: 'Liver extraction',
				description: 'Liver sampling for transcriptomics arrays',
				entity: dbnp.studycapturing.SamplingEvent)
                        .addToFields(samplingProtocolField)
			.addToFields(new TemplateField(
				name: 'Sample weight',
				unit: 'mg',
				type: TemplateFieldType.FLOAT))
			.with { if (!validate()) { errors.each { println it} } else save()}

			def bloodSamplingEventTemplate = new Template(
				name: 'Blood extraction',
				description: 'Blood extraction targeted at lipid assays',
	    		        entity: dbnp.studycapturing.SamplingEvent)
                        .addToFields(samplingProtocolField)
			.addToFields(new TemplateField(
				name: 'Sample volume',
				unit: 'ml',
				type: TemplateFieldType.FLOAT))
			.with { if (!validate()) { errors.each { println it} } else save()}


			// Add example studies
			if (!(grails.util.GrailsUtil.environment == GrailsApplication.ENV_TEST)) {
				println ".adding NuGO PPS3 leptin example study..."
				def mouseStudy = new Study(
					template: studyTemplate,
					title:"NuGO PPS3 mouse study leptin module",
					code:"PPS3_leptin_module",
					researchQuestion:"Leptin etc.",
					ecCode:"2007117.c",
					startDate: Date.parse('yyyy-MM-dd','2008-01-02'),
					externalStudyID: 1
				)
				.with { if (!validate()) { errors.each { println it} } else save()}

				mouseStudy.setFieldValue('Description', "C57Bl/6 mice were fed a high fat (45 en%) or low fat (10 en%) diet after a four week run-in on low fat diet.");// After 1 week 10 mice that received a low fat diet were given an IP leptin challenge and 10 mice of the low-fat group received placebo injections. The same procedure was performed with mice that were fed the high-fat diet. After 4 weeks the procedure was repeated. In total 80 mice were culled." )
				mouseStudy.save()

				def evLF = new Event(
					startTime: 3600,
					endTime: 3600 +7 * 24 * 3600,
					template: dietTreatmentTemplate
				)
				.setFieldValue( 'Diet','10% fat (palm oil)')
				.with { if (!validate()) { errors.each { println it} } else save()}

				def evHF = new Event(
					startTime: 3600,
					endTime: 3600 +7 * 24 * 3600,
					template: dietTreatmentTemplate
				)
				.setFieldValue( 'Diet','45% fat (palm oil)' )
				.with { if (!validate()) { errors.each { println it} } else save()}

				def evBV = new Event(
					startTime: 3600,
					endTime: 3600 +7 * 24 * 3600,
					template: boostTreatmentTemplate
				)
				.setFieldValue( 'Compound','Vehicle' )
				.with { if (!validate()) { errors.each { println it} } else save()}

				def evBL = new Event(
					startTime: 3600,
					endTime: 3600 +7 * 24 * 3600,
					template: boostTreatmentTemplate
				)
				.setFieldValue( 'Compound','Leptin' )
				.with { if (!validate()) { errors.each { println it} } else save()}

				def evLF4 = new Event(
					startTime: 3600,
					endTime: 3600 + 4 * 7 * 24 * 3600,
					template: dietTreatmentTemplate
				)
				.setFieldValue( 'Diet','10% fat (palm oil)')
				.with { if (!validate()) { errors.each { println it} } else save()}

				def evHF4 = new Event(
					startTime: 3600,
					endTime: 3600 + 4 * 7 * 24 * 3600,
					template: dietTreatmentTemplate
				)
				.setFieldValue( 'Diet','45% fat (palm oil)' )
				.with { if (!validate()) { errors.each { println it} } else save()}

				def evBV4 = new Event(
					startTime: 3600,
					endTime: 3600 + 4 * 7 * 24 * 3600,
					template: boostTreatmentTemplate
				)
				.setFieldValue( 'Compound','Vehicle' )
				.with { if (!validate()) { errors.each { println it} } else save()}

				def evBL4 = new Event(
					startTime: 3600,
					endTime: 3600 + 4 * 7 * 24 * 3600,
					template: boostTreatmentTemplate
				)
				.setFieldValue( 'Compound','Leptin' )
				.with { if (!validate()) { errors.each { println it} } else save()}

				def evS = new SamplingEvent(
					startTime: 3600 +7 * 24 * 3600,
					endTime: 3600 +7 * 24 * 3600,
					template: liverSamplingEventTemplate)
				.setFieldValue('Sample weight',5F)
				.with { if (!validate()) { errors.each { println it} } else save()}

				def evS4 = new SamplingEvent(
					startTime: 3600 +7 * 24 * 3600,
					endTime: 3600 +7 * 24 * 3600,
					template: liverSamplingEventTemplate)
				.setFieldValue('Sample weight',5F)
				.with { if (!validate()) { errors.each { println it} } else save()}

				// Add events to study
				mouseStudy
				.addToEvents(evLF)
				.addToEvents(evHF)
				.addToEvents(evBV)
				.addToEvents(evBL)
				.addToEvents(evLF4)
				.addToEvents(evHF4)
				.addToEvents(evBV4)
				.addToEvents(evBL4)
				.addToSamplingEvents(evS)
				.addToSamplingEvents(evS4)
				.save()

				def LFBV1 = new EventGroup(name:"10% fat + vehicle for 1 week")
				.addToEvents(evLF)
				.addToEvents(evBV)
				.addToEvents(evS)
				.with { if (!validate()) { errors.each { println it} } else save()}

				def LFBL1 = new EventGroup(name:"10% fat + leptin for 1 week")
				.addToEvents(evLF)
				.addToEvents(evBL)
				.addToEvents(evS)
				.with { if (!validate()) { errors.each { println it} } else save()}

				def HFBV1 = new EventGroup(name:"45% fat + vehicle for 1 week")
				.addToEvents(evHF)
				.addToEvents(evBV)
				.addToEvents(evS)
				.with { if (!validate()) { errors.each { println it} } else save()}

				def HFBL1 = new EventGroup(name:"45% fat + leptin for 1 week")
				.addToEvents(evHF)
				.addToEvents(evBL)
				.addToEvents(evS)
				.with { if (!validate()) { errors.each { println it} } else save()}

				def LFBV4 = new EventGroup(name:"10% fat + vehicle for 4 weeks")
				.addToEvents(evLF4)
				.addToEvents(evBV4)
				.addToEvents(evS4)
				.with { if (!validate()) { errors.each { println it} } else save()}

				def LFBL4 = new EventGroup(name:"10% fat + leptin for 4 weeks")
				.addToEvents(evLF4)
				.addToEvents(evBL4)
				.addToEvents(evS4)
				.with { if (!validate()) { errors.each { println it} } else save()}

				def HFBV4 = new EventGroup(name:"45% fat + vehicle for 4 weeks")
				.addToEvents(evHF4)
				.addToEvents(evBV4)
				.addToEvents(evS4)
				.with { if (!validate()) { errors.each { println it} } else save()}

				def HFBL4 = new EventGroup(name:"45% fat + leptin for 4 weeks")
				.addToEvents(evHF4)
				.addToEvents(evBL4)
				.addToEvents(evS4)
				.with { if (!validate()) { errors.each { println it} } else save()}

                // Add subjects and samples and compose EventGroups
				def x=1
				20.times {
					def currentSubject = new Subject(
						name: "A" + x++,
						species: mouseTerm,
						template: mouseTemplate,
					)
					.setFieldValue("Gender", "Male")
					.setFieldValue("Genotype", c57bl6Term)
					.setFieldValue("Age", 17)
					.setFieldValue("Cage", "" + (int)(x/2))
					.with { if (!validate()) { errors.each { println it} } else save(flush:true)}

					mouseStudy.addToSubjects(currentSubject)
					.with { if (!validate()) { errors.each { println it} } else save()}

					// Add subject to appropriate EventGroup
					if (x > 70) { HFBL4.addToSubjects(currentSubject).save() }
					else if (x > 60) { HFBV4.addToSubjects(currentSubject).save() }
					else if (x > 50) { LFBL4.addToSubjects(currentSubject).save() }
					else if (x > 40) { LFBV4.addToSubjects(currentSubject).save() }
					else if (x > 30) { HFBL1.addToSubjects(currentSubject).save() }
					else if (x > 20) { HFBV1.addToSubjects(currentSubject).save() }
					else if (x > 10) { LFBL1.addToSubjects(currentSubject).save() }
					else             { LFBV1.addToSubjects(currentSubject).save() }

				}

				// Add EventGroups to study
				mouseStudy
				.addToEventGroups(LFBV1)
				.addToEventGroups(LFBL1)
				.addToEventGroups(HFBV1)
				.addToEventGroups(HFBL1)
				.addToEventGroups(LFBV4)
				.addToEventGroups(LFBL4)
				.addToEventGroups(HFBV4)
				.addToEventGroups(HFBL4)

				// Add persons and publications to study
				def studyperson1 = new StudyPerson( person: person1, role: role1 ).save();
				def studyperson2 = new StudyPerson( person: person2, role: role2 ).save();

				mouseStudy
				.addToPersons( studyperson1 )
				.addToPersons( studyperson2 )
                                .addToPublications( publication1 )
                                .addToPublications( publication2 )
				.save()

				println ".adding NuGO PPSH example study..."

				def humanStudy = new Study(
					template: studyTemplate,
					title:"NuGO PPS human study",
					code:"PPSH",
					researchQuestion:"How much are fasting plasma and urine metabolite levels affected by prolonged fasting ?",
					description:"Human study",
					ecCode:"unknown",
					startDate: Date.parse('yyyy-MM-dd','2008-01-14'),
					externalStudyID: 2
				)
				.setFieldValue( 'Description', "Human study performed at RRI; centres involved: RRI, IFR, TUM, Maastricht U." )
				.with { if (!validate()) { errors.each { println it} } else save()}

				def rootGroup = new EventGroup(name: 'Root group');

				def fastingEvent = new Event(
					startTime: 3 * 24 * 3600 + 22 * 3600,
					endTime: 3 * 24 * 3600 + 30 * 3600,
					template: fastingTreatment)
				.setFieldValue('Fasting period','8h');


				def bloodSamplingEvent = new SamplingEvent(
					startTime: 3 * 24 * 3600 + 30 * 3600,
					endTime: 3 * 24 * 3600 + 30 * 3600,
					template: bloodSamplingEventTemplate)
				.setFieldValue('Sample volume',4.5F);

				rootGroup.addToEvents fastingEvent
				rootGroup.addToEvents bloodSamplingEvent
				rootGroup.save()

				def y = 1
				11.times {
				        def currentSubject = new Subject(
					      name: "" + y++,
					      species: humanTerm,
					      template: humanTemplate)
					.setFieldValue("Gender", (Math.random() > 0.5) ? "Male" : "Female")
					.setFieldValue("DOB", new java.text.SimpleDateFormat("dd-mm-yy").parse("01-02-19" + (10 + (int) (Math.random() * 80))))
					.setFieldValue("Age", 30)
					.setFieldValue("Height", Math.random() * 2F)
					.setFieldValue("Weight", Math.random() * 150F)
					.setFieldValue("BMI", 20 + Math.random() * 10F)
					.with { if (!validate()) { errors.each { println it} } else save()}

				        rootGroup.addToSubjects currentSubject
				        rootGroup.save()

				        def currentSample = new Sample(
					      name: currentSubject.name + '_B',
					      material: bloodTerm,
					      parentSubject: currentSubject,
					      parentEvent: bloodSamplingEvent);

				        humanStudy.addToSubjects(currentSubject).addToSamples(currentSample).with { if (!validate()) { errors.each { println it} } else save()}
				}

				humanStudy.addToEvents(fastingEvent)
				humanStudy.addToSamplingEvents(bloodSamplingEvent)
				humanStudy.addToEventGroups rootGroup


				// Add persons to study
				def studyperson3 = new StudyPerson( person: person1, role: role2 ).save();

				humanStudy
				.addToPersons( studyperson3 )
                                .addToPublications( publication2 )
				.save()

				/*
				// Add clinical data

				def lipidAssay = new dbnp.clinicaldata.ClinicalAssay(
					name: 'Lipid profile',
					approved: true
				).with { if (!validate()) { errors.each { println it} } else save()}

				def ldlMeasurement = new dbnp.clinicaldata.ClinicalMeasurement(
					name: 'LDL',
					unit: 'mg/dL',
					type: dbnp.data.FeatureType.QUANTITATIVE,
					referenceValues: '100 mg/dL',
					detectableLimit: 250,
					isDrug: false, isIntake: true, inSerum: true
				).with { if (!validate()) { errors.each { println it} } else save()}

				def hdlMeasurement = new dbnp.clinicaldata.ClinicalMeasurement(
					name: 'HDL',
					unit: 'mg/dL',
					type: dbnp.data.FeatureType.QUANTITATIVE,
					referenceValues: '50 mg/dL',
					detectableLimit: 100,
					isDrug: false, isIntake: true, inSerum: true
				).with { if (!validate()) { errors.each { println it} } else save()}

				lipidAssay.addToMeasurements ldlMeasurement
				lipidAssay.addToMeasurements hdlMeasurement

				def lipidAssayInstance = new dbnp.clinicaldata.ClinicalAssayInstance(
					assay: lipidAssay
				).with { if (!validate()) { errors.each { println it} } else save()}

				humanStudy.samples*.each {
					new dbnp.clinicaldata.ClinicalFloatData(
						assay: lipidAssayInstance,
						measurement: ldlMeasurement,
						sample: it.name,
						value: Math.round(Math.random()*ldlMeasurement.detectableLimit)
					).with { if (!validate()) { errors.each { println it} } else save()}

					new dbnp.clinicaldata.ClinicalFloatData(
						assay: lipidAssayInstance,
						measurement: hdlMeasurement,
						sample: it.name,
						value: Math.round(Math.random()*hdlMeasurement.detectableLimit)
					).with { if (!validate()) { errors.each { println it} } else save()}
				}
                                */
				// Add assay to study capture module

				def clinicalModule = new AssayModule(
					name: 'Clinical data',
					type: AssayType.CLINICAL_DATA,
					platform: 'clinical measurements',
					url: 'http://localhost:8080/gscf'
				).with { if (!validate()) { errors.each { println it} } else save()}

				def lipidAssayRef = new Assay(
					name: 'Lipid profiling',
					module: clinicalModule,
					externalAssayId: 0
				).with { if (!validate()) { errors.each { println it} } else save()}

				humanStudy.samples*.each {
					lipidAssayRef.addToSamples(it)
				}
				lipidAssayRef.save()

				humanStudy.addToAssays(lipidAssayRef);
				humanStudy.save()

       				mouseStudy.addToAssays(lipidAssayRef);
				mouseStudy.save()

			}
		}

		// Ontologies must be connected to the templatefields in runtime
                // because the Ontology.findByNcboId is not available otherwise
                TemplateEntity.getField( Subject.domainFields, 'species' ).ontologies = [Ontology.findByNcboId(1132)]
                TemplateEntity.getField( Sample.domainFields, 'material' ).ontologies = [Ontology.findByNcboId(1005)]

	}

	def destroy = {
	}
} 
