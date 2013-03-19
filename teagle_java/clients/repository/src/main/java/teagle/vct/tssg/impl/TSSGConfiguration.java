/**
 * 
 */
package teagle.vct.tssg.impl;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import teagle.vct.model.Configuration;
import teagle.vct.model.ConfigParamAtomic;
import teagle.vct.model.RepositoryException;

/**
 * @author sim
 *
 */
@XmlRootElement(name="configlet")
@XmlAccessorType(XmlAccessType.FIELD)
public class TSSGConfiguration extends TSSGEntity implements Configuration, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -580918860208630756L;

	protected static TSSGCache<TSSGConfiguration> cache = new TSSGCache<TSSGConfiguration>("configlet", new TSSGConfiguration[]{});

	@XmlElement(name="paramValue")
	protected String value = "";

	//@XmlElementWrapper(name="configurationParametersAtomic")
//	@XmlElement(name="configParamAtomic")
	private TSSGConfigParamAtomic configParamAtomic = new TSSGConfigParamAtomic();
	
	public TSSGConfiguration() {
	}

	protected TSSGConfiguration(Configuration configuration) {
		super(configuration);
		value = configuration.getValue();
		configParamAtomic = (TSSGConfigParamAtomic)configuration.getConfigParamAtomic();
		flag = true;
	}
	
	public static TSSGConfiguration find(String id) {
		return cache.find(id);
	}

	public static List<? extends Configuration> list() {
		return cache.list();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public TSSGConfiguration persist() {
		if (configParamAtomic != null)
			configParamAtomic.persist();
		return cache.persist(this);
	}

	@Override
	public void delete() throws RepositoryException {
		cache.delete(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public TSSGConfiguration resolve() {
		return id != null ? cache.find(id) : this;
	}
	
	@Override
	public boolean isModified() {
		return super.isModified() || (configParamAtomic != null && configParamAtomic.isModified());
	}

	@SuppressWarnings("unchecked")
	@Override
	public TSSGConfigurationInstance getInstance() {
		return new TSSGConfigurationInstance(this);
	}	

	@Override
	public ConfigParamAtomic getConfigParamAtomic() {
		if (configParamAtomic != null)
		{
			ConfigParamAtomic a = TSSGConfigParamAtomic.find(configParamAtomic.getId());
			if (a != null)
				return a;
		}

		return configParamAtomic;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
		flag = true;
	}
	
	@XmlRootElement(name="configletInstance")
	@XmlAccessorType(XmlAccessType.FIELD)
	@SuppressWarnings("unused")
	public static class TSSGConfigurationInstance implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2472241162493208979L;

		private String commonName;
		
		private String description;
		
		@XmlElement(name="paramValue")
		private String value;
		
		//@XmlElementWrapper(name="configurationParametersAtomic")
		@XmlElement(name="configParamAtomic.id")
		private String configParamAtomic;

		protected TSSGConfigurationInstance() {
		}
		
		protected TSSGConfigurationInstance(TSSGConfiguration configuration) {
			commonName = configuration.commonName;
			description = configuration.description;
			value = configuration.value;
			if (configuration.configParamAtomic != null)
				configParamAtomic = configuration.configParamAtomic.getId();
		}
	}
	
	@Override
	public String getValue() {
		return value;
	}

	@Override
	public boolean isReferenceArray() {
		return getConfigParamAtomic().getType().equals("reference-array");
	}

	@Override
	public boolean isReference() {
		return getConfigParamAtomic().getType().startsWith("reference");
	}

	@Override
	public void setConfigParamatomic(ConfigParamAtomic param)
	{
		this.configParamAtomic = (TSSGConfigParamAtomic)param;
		flag = true;
	}
}
