using UnityEngine;
using System;

namespace DeepNight.SDK
{
    /// <summary>
    /// DEEP NIGHT SDK - Unity Bridge for DAP Core.
    /// Provides access to native audio processing features in Unity.
    /// </summary>
    public class DeepNightDapBridge : MonoBehaviour
    {
        private static AndroidJavaObject _nativeInterface;

        void Awake()
        {
            if (Application.platform == RuntimePlatform.Android)
            {
                try
                {
                    using (var javaClass = new AndroidJavaClass("com.deepnight.sdk.dap.DapNativeInterface"))
                    {
                        _nativeInterface = javaClass.GetStatic<AndroidJavaObject>("INSTANCE");
                    }
                    Debug.Log("DeepNight SDK: DAP Native Interface initialized.");
                }
                catch (Exception e)
                {
                    Debug.LogError($"DeepNight SDK: Failed to initialize DAP Native Interface: {e.message}");
                }
            }
        }

        /// <summary>
        /// Detects if voice is active in the provided audio data.
        /// </summary>
        public bool IsVoiceActive(byte[] audioData, float threshold = 0.05f)
        {
            if (_nativeInterface == null) return false;
            return _nativeInterface.Call<bool>("isVoiceActive", audioData, audioData.Length, threshold);
        }

        /// <summary>
        /// Example of processing FFT. Output arrays must be pre-allocated.
        /// </summary>
        public void ProcessFft(byte[] audioData, float[] outReal, float[] outImag, int[] logIndices)
        {
            if (_nativeInterface == null) return;
            _nativeInterface.Call("processFft", audioData, audioData.Length, outReal, outImag, outReal.Length, logIndices);
        }
    }
}
