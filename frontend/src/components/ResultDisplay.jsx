import React from 'react';
import '../styles/ResultDisplay.css';

const ResultDisplay = ({ result, error, onReset }) => {
  const downloadPDF = () => {
    if (result) {
      // Convert base64 to blob and download
      const byteCharacters = atob(result);
      const byteNumbers = new Array(byteCharacters.length);
      for (let i = 0; i < byteCharacters.length; i++) {
        byteNumbers[i] = byteCharacters.charCodeAt(i);
      }
      const byteArray = new Uint8Array(byteNumbers);
      const blob = new Blob([byteArray], { type: 'application/pdf' });
      
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'generated-pdf.pdf';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
    }
  };

  const copyToClipboard = () => {
    if (result) {
      navigator.clipboard.writeText(result);
      alert('Base64 data copied to clipboard!');
    }
  };

  if (error) {
    return (
      <div className="result-container error">
        <h3>Error</h3>
        <p className="error-message">{error}</p>
        <button onClick={onReset} className="reset-btn">
          Try Again
        </button>
      </div>
    );
  }

  if (result) {
    return (
      <div className="result-container success">
        <h3>PDF Generated Successfully!</h3>
        
        <div className="result-actions">
          <button onClick={downloadPDF} className="download-btn">
            Download PDF
          </button>
          <button onClick={copyToClipboard} className="copy-btn">
            Copy Base64
          </button>
          <button onClick={onReset} className="reset-btn">
            Generate Another
          </button>
        </div>

        <div className="base64-preview">
          <h4>Base64 Data Preview:</h4>
          <textarea 
            value={result} 
            readOnly 
            rows="4"
            className="base64-text"
          />
        </div>
      </div>
    );
  }

  return null;
};

export default ResultDisplay;