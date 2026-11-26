import { handleAxiosError } from './client';

export const unwrap = async (requestFn) => {
  try {
    const response = await requestFn();
    return response.data;
  } catch (error) {
    throw new Error(handleAxiosError(error));
  }
};

export default unwrap;
