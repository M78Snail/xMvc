package bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

public class Param {

	public static final String SEPARATOR = String.valueOf((char) 29);
	private List<FormParam> formParamList;

	private List<FileParam> fileParamList;

	public Param(List<FormParam> formParamList) {
		this.formParamList = formParamList;
	}

	public Param(List<FormParam> formParamList, List<FileParam> fileParamList) {
		this.formParamList = formParamList;
		this.fileParamList = fileParamList;
	}

	public Map<String, Object> getFieldMap() {
		Map<String, Object> fieldMap = new HashMap<String, Object>();
		if (CollectionUtils.isNotEmpty(formParamList)) {
			for (FormParam formParam : formParamList) {
				String fieldName = formParam.getFieldName();
				Object fieldValue = formParam.getFieldValue();

				if (fieldMap.containsKey(fieldName)) {
					fieldValue = fieldMap.get(fieldName) + Param.SEPARATOR + fieldValue;
				}

				fieldMap.put(fieldName, fieldValue);
			}
		}
		return fieldMap;
	}

	public Map<String, List<FileParam>> getFileMap() {
		Map<String, List<FileParam>> fileMap = new HashMap<String, List<FileParam>>();
		if (CollectionUtils.isNotEmpty(fileParamList)) {
			for (FileParam fileParam : fileParamList) {
				String fieldName = fileParam.getFieldName();
				List<FileParam> fileParamList = null;
				if (fileMap.containsKey(fieldName)) {
					fileParamList = fileMap.get(fieldName);
				} else {
					fileParamList = new ArrayList<FileParam>();
				}
				fileParamList.add(fileParam);
				fileMap.put(fieldName, fileParamList);
			}
		}
		return fileMap;
	}

	public List<FileParam> getFileList(String fieldName) {
		return getFileMap().get(fieldName);
	}

	public FileParam getFile(String fieldName) {
		List<FileParam> fileParamList = getFileList(fieldName);
		if (CollectionUtils.isNotEmpty(fileParamList) && fileParamList.size() == 1) {
			return fileParamList.get(0);
		}
		return null;
	}

	public long getLong(String name) {
		return Long.parseLong((String) getFieldMap().get(name));
	}

	public String getString(String name) {
		return (String) getFieldMap().get(name);
	}

	public boolean isEmpty() {
		return CollectionUtils.isEmpty(formParamList) && CollectionUtils.isEmpty(fileParamList);
	}
}
