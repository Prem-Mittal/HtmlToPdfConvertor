import React, { useState } from 'react';
import FileUpload from './components/FileUpload';
import ResultDisplay from './components/ResultDisplay';
import { uploadPdfFiles } from './services/api';
import './styles/App.css';

function App() {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);

  const handleUpload = async (formData) => {
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      console.log('Uploading files with formData:', formData);
      const response = await uploadPdfFiles(formData);
      console.log('Response from server:', response);
      setResult(response);
    } catch (err) {
      setError(err.message || 'An error occurred while generating the PDF');
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setResult(null);
    setError(null);
  };

  return (
    <div className="app">
      <div className="container">
        {!result && !error && (
          <FileUpload onUpload={handleUpload} loading={loading} />
        )}
        
        <ResultDisplay 
          result={result} 
          error={error} 
          onReset={handleReset} 
        />
      </div>
    </div>
  );
}

export default App;