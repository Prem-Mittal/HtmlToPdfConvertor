import axios from 'axios';
const  API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
export const uploadPdfFiles = async (formData) => {
  try {
    console.log('Uploading files to:', `${API_BASE_URL}/v2/communications/download_base_url`);
    const response = await axios.post(`${API_BASE_URL}/v2/communications/download_base_url`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  } catch (error) {
    throw new Error(error.response?.data || 'Upload failed');
  }
};