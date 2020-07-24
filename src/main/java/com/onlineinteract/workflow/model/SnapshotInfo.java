package com.onlineinteract.workflow.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnapshotInfo {
	private Map<String, Domain> domains = new HashMap<>();
	private String name;
	private String _id;

	public Map<String, Domain> getDomains() {
		return domains;
	}

	public void setDomains(Map<String, Domain> domains) {
		this.domains = domains;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_id == null) ? 0 : _id.hashCode());
		result = prime * result + ((domains == null) ? 0 : domains.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SnapshotInfo other = (SnapshotInfo) obj;
		if (_id == null) {
			if (other._id != null)
				return false;
		} else if (!_id.equals(other._id))
			return false;
		if (domains == null) {
			if (other.domains != null)
				return false;
		} else if (!domains.equals(other.domains))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public static class Domain {
		private String collection;
		private String topic;
		private String cron;
		private List<Version> versions;

		public Domain() {
		}

		public String getCollection() {
			return collection;
		}

		public void setCollection(String collection) {
			this.collection = collection;
		}

		public String getTopic() {
			return topic;
		}

		public void setTopic(String topic) {
			this.topic = topic;
		}

		public String getCron() {
			return cron;
		}

		public void setCron(String cron) {
			this.cron = cron;
		}

		public List<Version> getVersions() {
			return versions;
		}

		public void setVersions(List<Version> versions) {
			this.versions = versions;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((collection == null) ? 0 : collection.hashCode());
			result = prime * result + ((cron == null) ? 0 : cron.hashCode());
			result = prime * result + ((topic == null) ? 0 : topic.hashCode());
			result = prime * result + ((versions == null) ? 0 : versions.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Domain other = (Domain) obj;
			if (collection == null) {
				if (other.collection != null)
					return false;
			} else if (!collection.equals(other.collection))
				return false;
			if (cron == null) {
				if (other.cron != null)
					return false;
			} else if (!cron.equals(other.cron))
				return false;
			if (topic == null) {
				if (other.topic != null)
					return false;
			} else if (!topic.equals(other.topic))
				return false;
			if (versions == null) {
				if (other.versions != null)
					return false;
			} else if (!versions.equals(other.versions))
				return false;
			return true;
		}
	}

	public static class Version {
		private long version;
		private long beginSnapshotOffset;
		private long endSnapshotOffset;

		public Version() {
		}

		public long getVersion() {
			return version;
		}

		public void setVersion(long version) {
			this.version = version;
		}

		public long getBeginSnapshotOffset() {
			return beginSnapshotOffset;
		}

		public void setBeginSnapshotOffset(long beginSnapshotOffset) {
			this.beginSnapshotOffset = beginSnapshotOffset;
		}

		public long getEndSnapshotOffset() {
			return endSnapshotOffset;
		}

		public void setEndSnapshotOffset(long endSnapshotOffset) {
			this.endSnapshotOffset = endSnapshotOffset;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (beginSnapshotOffset ^ (beginSnapshotOffset >>> 32));
			result = prime * result + (int) (endSnapshotOffset ^ (endSnapshotOffset >>> 32));
			result = prime * result + (int) (version ^ (version >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Version other = (Version) obj;
			if (beginSnapshotOffset != other.beginSnapshotOffset)
				return false;
			if (endSnapshotOffset != other.endSnapshotOffset)
				return false;
			if (version != other.version)
				return false;
			return true;
		}
	}
}
