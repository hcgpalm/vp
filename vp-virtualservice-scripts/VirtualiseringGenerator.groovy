#!/usr/bin/env groovy

package se.skltp.virtualservices

import groovy.io.FileType
import java.nio.file.*

org.apache.commons.io.FileUtils

@Grab(group='commons-io', module='commons-io', version='2.4')
import org.apache.commons.io.FileUtils

@Grab(group='dom4j', module='dom4j', version='1.6.1')
import org.dom4j.io.SAXReader

class VirtualService{

	static final String versionNrRegex = '''(?:\\d*\\.)?\\d+'''

	//[clinicalprocess,activityprescription,actoutcome,GetMedicationHistoryResponder,2]
	def serviceInteractionNameSpaceArr = ""

	//[clinicalprocess,activityprescription,actoutcome,GetMedicationHistory,2,rivtabp21]
	def serviceContractNameSpaceArr = ""

	def wsdlFileName = ""
	def xsdFileName = ""
	def serviceDomainVersion = ""

	VirtualService(serviceInteractionNameSpace, serviceContractNamespace, wsdlFileName, xsdFileName, serviceDomainVersion){
		serviceInteractionNameSpace = serviceInteractionNameSpace - 'urn:riv:'
		serviceInteractionNameSpaceArr = serviceInteractionNameSpace.split(':')
		serviceContractNamespace = serviceContractNamespace - 'urn:riv:'
		serviceContractNameSpaceArr = serviceContractNamespace.split(':')

		this.wsdlFileName = wsdlFileName
		this.xsdFileName = xsdFileName
		this.serviceDomainVersion = serviceDomainVersion
	}

	def getHttpsAddress(){
		//https://${TP_HOST}:${TP_PORT}/${TP_BASE_URI}/clinicalprocess/healthcond/basic/GetObservation/1/rivtabp21
		return '''https://${TP_HOST}:${TP_PORT}/${TP_BASE_URI}/''' + serviceInteractionNameSpaceArr.join('/')
	}

	def getHttpAddress(){
		//https://${TP_HOST}:${TP_PORT_HTTP}/${TP_BASE_URI}/clinicalprocess/healthcond/basic/GetObservation/1/rivtabp21
		return '''http://${TP_HOST}:${TP_PORT_HTTP}/${TP_BASE_URI}/''' + serviceInteractionNameSpaceArr.join('/')
	}

	def getFlowName(){
		//clinicalprocess-healthcond-basic-GetObservation-1-virtualisering
		return serviceContractNameSpaceArr.join('-') + '-virtualisering'
	}

	def getFeatureKeepAlive(){
		//${feature.keepalive.urn.riv.clinicalprocess.healthcond.basic.GetObservationResponder.1:${feature.keepalive}}
		return '''${feature.keepalive.urn.riv.''' + serviceContractNameSpaceArr.join('.') + ''':${feature.keepalive}}'''
	}

	def getFeatureResponseTimeout(){
		// ${feature.featureresponsetimeout.clinicalprocess.healthcond:basic:${SERVICE_TIMEOUT_MS}}
		return '''${feature.featureresponsetimeout.urn.riv.''' + serviceContractNameSpaceArr.join('.') + ''':${SERVICE_TIMEOUT_MS}}'''
	}

	def getServiceInteractionNameSpace(){
		//urn:riv:clinicalprocess:healthcond:basic:GetObservation:1:rivtabp21
		return 'urn:riv:' + serviceInteractionNameSpaceArr.join(':')
	}

	def getWsdlServiceName(){
		//GetObservationResponderService
		return serviceContractNameSpaceArr[-2] + "Service"
	}

	def getWsdlServiceMethod(){
		//GetObservationInteraction_1.0_RIVTABP21.wsdl will return GetObservationInteraction
		return wsdlFileName.split('_')[0]
	}

	def getWsdlFilePath(){
		//classpath:/schemas/interactions/GetObservationInteraction/GetObservationInteraction_1.0_RIVTABP21.wsdl
		return '''classpath:/schemas/interactions/''' + wsdlFileName.split('_')[0] + '/' + wsdlFileName
	}

	def getVirtualizationArtifactId(){
		//clinicalprocess.healthcond.basic.2.0.GetObservationInteraction.2.0.virtualisering
		return serviceInteractionNameSpaceArr[0..2].join('-') +
		'-' +
		serviceDomainVersion +
		'-' +
		getWsdlServiceMethod() +
		'-virtualisering'
	}

	def getServiceContractMajorAndMinorVersion(){
		//GetVaccinationHistoryResponder_2.0.xsd will return 2.0
		return ( xsdFileName =~ versionNrRegex )[0]
	}
}

def getAllDirectoriesMatching(direcory, pattern){
	def dirsFound = []
	direcory?.traverse(type:FileType.DIRECTORIES, nameFilter: ~pattern){ dirFound -> dirsFound << dirFound }
	dirsFound.each { dirFound -> println "Directory to process: ${dirFound}" }
	return dirsFound
}

def getAllFilesMatching(direcory, pattern){
	def filesFound = []
	direcory?.traverse(type:FileType.FILES, nameFilter: ~pattern){ fileFound -> filesFound << fileFound }
	filesFound.each { fileFound -> println "File to process: ${fileFound.name}" }
	return filesFound
}

def getNameSpaces(file, text){
	def namespaceFound = 'No namespace found'

	new SAXReader().read(file).getRootElement().declaredNamespaces().grep(~/.*urn:riv.*/).each{ namespace ->
		if(namespace.text.contains(text)){ namespaceFound = namespace.text }
	}
	return namespaceFound
}

def buildVirtualServices(serviceInteractionDirectories, targetDir, vpVersion, serviceDomainVersion){

	serviceInteractionDirectories.each { serviceInteractionDirectory ->

		def wsdlFiles = getAllFilesMatching(serviceInteractionDirectory, /.*\.wsdl/)
		def serviceInteractionNameSpace = getNameSpaces(wsdlFiles[0], 'rivtabp')

		def xsdFiles = getAllFilesMatching(serviceInteractionDirectory, /.*\.xsd/)
		def serviceContractNameSpace = getNameSpaces(xsdFiles[0], 'Responder')

		def virtualService = new VirtualService(serviceInteractionNameSpace,serviceContractNameSpace,
			wsdlFiles[0].name, xsdFiles[0].name, serviceDomainVersion)

		def mvnCommand = """mvn archetype:generate
		-DinteractiveMode=false
		-DarchetypeArtifactId=vp-virtualservice-template
		-DarchetypeGroupId=se.skltp.vp
		-DarchetypeVersion=${vpVersion}
		-Duser.dir=${targetDir}
		-DgroupId=N/A
		-DartifactId=${virtualService.getWsdlServiceMethod()}
		-DvirtualiseringArtifactId=${virtualService.getVirtualizationArtifactId()}
		-Dversion=${virtualService.getServiceContractMajorAndMinorVersion()}
		-DhttpsEndpointAdress=${virtualService.getHttpsAddress()}
		-DhttpEndpointAdress=${virtualService.getHttpAddress()}
		-DflowName=${virtualService.getFlowName()}
		-DfeatureKeepaliveValue=${virtualService.getFeatureKeepAlive()}
		-DfeatureResponseTimeoutValue=${virtualService.getFeatureResponseTimeout()}
		-DserviceMethod=${virtualService.getWsdlServiceMethod()}
		-DserviceWsdlFileDir=${virtualService.getWsdlFilePath()}
		-DserviceNamespace=${virtualService.getServiceInteractionNameSpace()}
		-DwsdlServiceName=${virtualService.getWsdlServiceName()}
	"""

		println mvnCommand

		def process = mvnCommand.execute()
		process.waitFor()

		// Obtain status and output
		println "RETURN CODE: ${ process.exitValue()}"
		println "STDOUT: ${process.in.text}"
	}
}

//Script start

if( args.size() < 3){
	println "This tool generates service virtualising components based on service interactions found in sourceDir. They are generated in the dir where script is executed."
	println "Point sourceDir to the schemas dir containing:"
	println "core_components"
	println "interactions"
	println ""
	println "To be able to run this tool you need to have maven (http://maven.apache.org/) installed."
	println ""
	println "Required parameters: vp version [vpVersion] source directory [sourceDir] \n"
	println "PARAMETERS DESCRIPTION:"
	println "[vpVersion] is the version of VP you would like virtual services for, e.g 2.2.9"
	println "[sourceDir] is the base direcory where this script will start working to look for servivce interactions, e.g /repository/rivta/ServiceInteractions/riv/crm/scheduling/trunk "
	println "[serviceDomainVersion] version of servicedomain, e.g 3.0.0 or 2.0-RC1"
	println ""
	println "OUTPUT:"
	println "New maven folders containing service interactions"
	return
}

def vpVersion = args[0]
def sourceDir = new File(args[1])
def serviceDomainVersion = args[2]
def targetDir = new File(".").getAbsolutePath()

println "-------------------------------------------------"
println "VP Version: ${vpVersion}"
println "Source directory: ${sourceDir}"
println "Target directory: ${targetDir}"
println "-------------------------------------------------"

new File("pom.xml").delete()
new File("${targetDir}/pom.xml") << new File("pomtemplate.xml").asWritable()

def serviceInteractionDirectories = getAllDirectoriesMatching(sourceDir,/.*Interaction$/)

buildVirtualServices(serviceInteractionDirectories, targetDir, vpVersion, serviceDomainVersion)

println ""
println ""
println "NOTE! Run mvn clean package to build deployable jar-files for service platform without adding to your local repo"
