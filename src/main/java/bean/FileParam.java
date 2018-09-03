package bean;

import java.io.InputStream;

public class FileParam {
	private String fileName;
	private String fieldName;
	private long fileSize;

	private String contentType;
	private InputStream inputStream;
	public String getFileName() {
		return fileName;
	}
	public String getFieldName() {
		return fieldName;
	}
	public long getFileSize() {
		return fileSize;
	}
	public String getContentType() {
		return contentType;
	}
	public InputStream getInputStream() {
		return inputStream;
	}
	public FileParam(String fileName, String fieldName, long fileSize, String contentType, InputStream inputStream) {
		super();
		this.fileName = fileName;
		this.fieldName = fieldName;
		this.fileSize = fileSize;
		this.contentType = contentType;
		this.inputStream = inputStream;
	}
	
	
}
