import React, { useState } from 'react';
import '../styles/FileUpload.css';

const FileUpload = ({ onUpload, loading }) => {
  const [files, setFiles] = useState({
    headerFooterCss: null,
    frontendAdditionalCss: null,
    frontendCss: null
  });
  const [textData, setTextData] = useState('');

  const handleFileChange = (fileType, event) => {
    const file = event.target.files[0];
    setFiles(prev => ({
      ...prev,
      [fileType]: file
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    
    // Validate files
    if (!files.headerFooterCss || !files.frontendAdditionalCss || !files.frontendCss) {
      alert('Please select all three CSS files');
      return;
    }

    if (!textData.trim()) {
      alert('Please enter PDF creation data');
      return;
    }

    // Create FormData
    const formData = new FormData();
    formData.append('headerfootercssFile', files.headerFooterCss);
    formData.append('frontendadditionalcssfile', files.frontendAdditionalCss);
    formData.append('frontendcssfile', files.frontendCss);
    formData.append('pdfCreationDto', textData);

    onUpload(formData);
  };

  return (
    <div className="file-upload-container">
      <h2>PDF Generator</h2>
      
      <form onSubmit={handleSubmit} className="upload-form">
        <div className="file-inputs">
          <div className="file-input-group">
            <label htmlFor="headerFooterCss">Header/Footer CSS File:</label>
            <input
              type="file"
              id="headerFooterCss"
              accept=".css,.txt"
              onChange={(e) => handleFileChange('headerFooterCss', e)}
              disabled={loading}
            />
            {files.headerFooterCss && (
              <span className="file-name">{files.headerFooterCss.name}</span>
            )}
          </div>

          <div className="file-input-group">
            <label htmlFor="frontendAdditionalCss">Frontend Additional CSS File:</label>
            <input
              type="file"
              id="frontendAdditionalCss"
              accept=".css,.txt"
              onChange={(e) => handleFileChange('frontendAdditionalCss', e)}
              disabled={loading}
            />
            {files.frontendAdditionalCss && (
              <span className="file-name">{files.frontendAdditionalCss.name}</span>
            )}
          </div>

          <div className="file-input-group">
            <label htmlFor="frontendCss">Frontend CSS File:</label>
            <input
              type="file"
              id="frontendCss"
              accept=".css,.txt"
              onChange={(e) => handleFileChange('frontendCss', e)}
              disabled={loading}
            />
            {files.frontendCss && (
              <span className="file-name">{files.frontendCss.name}</span>
            )}
          </div>
        </div>

        <div className="text-input-group">
          <label htmlFor="pdfData">PDF Creation Data (JSON):</label>
          <textarea
            id="pdfData"
            value={textData}
            onChange={(e) => setTextData(e.target.value)}
            placeholder='{"title":"Sample PDF","content":"<html><body><h1>Hello</h1></body></html>","pageSize":"A4"}'
            rows="6"
            disabled={loading}
          />
        </div>

        <button 
          type="submit" 
          className="submit-btn"
          disabled={loading}
        >
          {loading ? 'Generating PDF...' : 'Generate PDF'}
        </button>
      </form>
    </div>
  );
};

export default FileUpload;