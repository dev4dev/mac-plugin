package fr.edf.jenkins.plugins.mac

import org.apache.commons.lang.StringUtils
import org.kohsuke.stapler.AncestorInPath
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.DataBoundSetter
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.verb.POST

import fr.edf.jenkins.plugins.mac.ssh.key.verifiers.MacHostKeyVerifier
import fr.edf.jenkins.plugins.mac.util.FormUtils
import hudson.Extension
import hudson.model.Describable
import hudson.model.Descriptor
import hudson.model.Item
import hudson.model.Label
import hudson.model.labels.LabelAtom
import hudson.util.FormValidation
import hudson.util.ListBoxModel
import hudson.util.FormValidation.Kind
import jenkins.model.Jenkins

/**
 * Configuration of a Mac SSH Connection
 * @author Mathieu DELROCQ
 *
 */
class MacHost implements Describable<MacHost> {

    String host
    String credentialsId
    Integer port
    Integer maxUsers
    Integer connectionTimeout
    Integer readTimeout
    Integer kexTimeout
    Integer agentConnectionTimeout
    Integer maxTries
    Boolean disabled
    Boolean copySSHEnvFile
    Boolean copySSHEnv
    Boolean uploadKeychain = Boolean.FALSE
    Boolean uploadNetRC = Boolean.FALSE
    String labelString
    String fileCredentialsId
    String netRCFileCredentialsId
    List<MacEnvVar> envVars = new ArrayList()
    MacHostKeyVerifier macHostKeyVerifier
    transient Set<LabelAtom> labelSet

    @DataBoundConstructor
    MacHost(String host, String credentialsId, Integer port, Integer maxUsers, Integer connectionTimeout, Integer readTimeout, 
            Integer agentConnectionTimeout, Boolean disabled, Integer maxTries, String labelString, Boolean uploadKeychain, 
            String fileCredentialsId, Boolean uploadNetRC, String netRCFileCredentialsId, List<MacEnvVar> envVars, String key,
            Boolean copySSHEnvFile, Boolean copySSHEnv) {
        this.host = host
        this.credentialsId = credentialsId
        this.port = port
        this.maxUsers = maxUsers
        this.connectionTimeout = connectionTimeout
        this.readTimeout = readTimeout
        this.kexTimeout = new Integer(0)
        this.agentConnectionTimeout = agentConnectionTimeout
        this.disabled = disabled
        this.maxTries = maxTries
        this.labelString = labelString
        this.envVars = envVars
        this.uploadKeychain = uploadKeychain ?: Boolean.FALSE
        this.uploadNetRC = uploadNetRC ?: Boolean.FALSE
        this.fileCredentialsId = fileCredentialsId
        this.macHostKeyVerifier = new MacHostKeyVerifier(key)
        this.copySSHEnvFile = copySSHEnvFile
        this.copySSHEnv = copySSHEnv
        this.netRCFileCredentialsId = netRCFileCredentialsId
        labelSet = Label.parse(StringUtils.defaultIfEmpty(labelString, ""))
    }
    
    String getKey() {
        null != this.macHostKeyVerifier ? macHostKeyVerifier.getKey() : ""
    }

    @DataBoundSetter
    void setHost(String host) {
        this.host = host
    }

    @DataBoundSetter
    void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId
    }

    @DataBoundSetter
    void setPort(Integer port) {
        this.port = port
    }

    @DataBoundSetter
    void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers
    }

    @DataBoundSetter
    void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout
    }

    @DataBoundSetter
    void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout
    }

    @DataBoundSetter
    void setKexTimeout(Integer kexTimeout) {
        this.kexTimeout = kexTimeout
    }

    @DataBoundSetter
    void setAgentConnectionTimeout(Integer agentConnectionTimeout) {
        this.agentConnectionTimeout = agentConnectionTimeout
    }

    @DataBoundSetter
    void setDisabled(Boolean disabled) {
        this.disabled = disabled
    }

    @DataBoundSetter
    void setMaxTries(Integer maxTries) {
        this.maxTries = maxTries
    }

    @DataBoundSetter
    void setLabelString(String labelString) {
        this.labelString = labelString
    }

    @DataBoundSetter
    void setEnvVars(List<MacEnvVar> envVars) {
        this.envVars = envVars
    }

    @DataBoundSetter
    void setCopySSHEnvFile(Boolean copySSHEnvFile) {
        this.copySSHEnvFile = copySSHEnvFile
    }

    @DataBoundSetter
    void setCopySSHEnv(Boolean copySSHEnv) {
        this.copySSHEnv = copySSHEnv
    }

    @DataBoundSetter
    void setUploadKeychain(Boolean uploadKeychain = Boolean.FALSE) {
        this.uploadKeychain = uploadKeychain
    }

    @DataBoundSetter
    void setFileCredentialsId(String fileCredentialsId) {
        this.fileCredentialsId = fileCredentialsId
    }

    @DataBoundSetter
    void setUploadNetRC(Boolean uploadNetRC = Boolean.FALSE) {
        this.uploadNetRC = uploadNetRC
    }

    @DataBoundSetter
    void setNetRCFileCredentialsId(String netRCFileCredentialsId) {
        this.netRCFileCredentialsId = netRCFileCredentialsId
    }

    @Override
    Descriptor<MacHost> getDescriptor() {
        return Jenkins.get().getDescriptorOrDie(this.getClass())
    }

    Set<LabelAtom> getLabelSet() {
        return Label.parse(StringUtils.defaultIfEmpty(this.labelString, ""))
    }

    /**
     * Descriptor of a Mac Host for Jenkins UI
     * @see src\main\resources\fr\jenkins\plugins\mac\MacHost\config.groovy
     * @author mathieu.delrocq
     *
     */
    @Extension
    static class DescriptorImpl extends Descriptor<MacHost> {

        /**
         * {@inheritDoc}
         */
        @Override
        String getDisplayName() {
            return Messages.Host_DisplayName()
        }

        /**
         * Check if the value of host field is correct
         * @param value
         * @return FormValidation
         */
        @POST
        FormValidation doCheckHost(@QueryParameter String value) {
            def validation = FormUtils.validateHost(value)
            if (validation.kind == Kind.OK) {
                validation = FormUtils.validateNotEmpty(value, Messages.Host_HostRequired())
            }
            return validation
        }

        /**
         * Return ListBoxModel of existing credentials
         * @param host
         * @param credentialsId
         * @param context
         * @return ListBoxModel
         */
        @POST
        ListBoxModel doFillCredentialsIdItems(@QueryParameter String host,
                @QueryParameter String credentialsId, @AncestorInPath Item ancestor) {
            return FormUtils.newMacHostCredentialsItemsListBoxModel(host, credentialsId, ancestor)
        }

        /**
         * Return ListBoxModel of existing keychains
         * @param credentialsId
         * @param context
         * @return ListBoxModel
         */
        @POST
        ListBoxModel doFillFileCredentialsIdItems(@QueryParameter String fileCredentialsId, @AncestorInPath Item ancestor) {
            return FormUtils.newFileCredentialsItemsListBoxModel(fileCredentialsId, ancestor)
        }

        /**
         * Return ListBoxModel of existing files
         * @param credentialsId
         * @param context
         * @return ListBoxModel
         */
        @POST
        ListBoxModel doFillNetRCFileCredentialsIdItems(@QueryParameter String netRCFileCredentialsId, @AncestorInPath Item ancestor) {
            return FormUtils.newFileCredentialsItemsListBoxModel(netRCFileCredentialsId, ancestor)
        }

        /**
         * Verify the connection to the Mac machine 
         * @param host
         * @param port
         * @param credentialsId
         * @param context
         * @return ok if connection, ko if error
         */
        @POST
        FormValidation doVerifyConnection(@QueryParameter String host, @QueryParameter Integer port,
                @QueryParameter String credentialsId, @QueryParameter String key, @AncestorInPath Item context) {
            return FormUtils.verifyConnection(host, port, credentialsId, key, context)
        }
        
        /**
         * Check the validity of the given key
         * @param key
         * @return ok if valid, error with exception message if not
         */
        @POST
        public FormValidation doCheckKey(@QueryParameter String key) {
            return FormUtils.verifyHostKey(key)
        }
    }
}
