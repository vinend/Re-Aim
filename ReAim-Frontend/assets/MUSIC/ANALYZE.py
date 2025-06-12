import argparse
import json
import os
import librosa
import numpy as np

# Analyze audio for rhythm game: tempo, beat times, and onset times
def analyze(file_path, hit_window, subdivisions):
    y, sr = librosa.load(file_path, sr=None)
    tempo, beat_frames = librosa.beat.beat_track(y=y, sr=sr)
    beat_times = librosa.frames_to_time(beat_frames, sr=sr)

    onset_env = librosa.onset.onset_strength(y=y, sr=sr)
    onset_frames = librosa.onset.onset_detect(onset_envelope=onset_env, sr=sr)
    onset_times = librosa.frames_to_time(onset_frames, sr=sr)

    # Compute RMS envelope and detect amplitude spikes as sound spikes
    rms = librosa.feature.rms(y=y)[0]
    rms_peaks = librosa.util.peak_pick(
        rms, pre_max=3, post_max=3, pre_avg=3, post_avg=3,
        delta=rms.mean(), wait=5
    )
    rms_peak_times = librosa.frames_to_time(rms_peaks, sr=sr)

    # Compute ideal beat times by aligning each beat to nearest strong onset
    ideal_beat_times = []
    confidence_scores = []
    for bt in beat_times:
        idx = np.argmin(np.abs(onset_times - bt))
        onset_time = float(onset_times[idx])
        # Score: combine onset strength and RMS at this frame
        onset_frame = onset_frames[idx] if idx < len(onset_frames) else librosa.time_to_frames(onset_time, sr=sr)
        onset_strength = onset_env[onset_frame] if onset_frame < len(onset_env) else 0
        rms_frame = librosa.time_to_frames(onset_time, sr=sr)
        rms_strength = rms[rms_frame] if rms_frame < len(rms) else 0
        # Normalize scores
        onset_score = onset_strength / (np.max(onset_env) + 1e-6)
        rms_score = rms_strength / (np.max(rms) + 1e-6)
        confidence = float(0.7 * onset_score + 0.3 * rms_score)
        ideal_beat_times.append(onset_time)
        confidence_scores.append(confidence)

    # Generate tap targets around each ideal beat, include confidence
    tap_targets = [
        {'time': t, 'window': [t - hit_window, t + hit_window], 'confidence': float(round(c, 3))}
        for t, c in zip(ideal_beat_times, confidence_scores)
        if c > 0.2  # filter out weak beats
    ]
    # Generate spike targets for RMS peaks not near strong beats
    spike_targets = [
        {'time': sp, 'window': [sp - hit_window, sp + hit_window]}
        for sp in rms_peak_times
        if min(abs(sp - t) for t in ideal_beat_times) > hit_window
    ]
    # Generate subdivisions between beats
    sub_div_times = []
    if subdivisions > 1 and len(ideal_beat_times) > 1:
        for i in range(len(ideal_beat_times) - 1):
            start, end = ideal_beat_times[i], ideal_beat_times[i+1]
            for s in range(1, subdivisions):
                sub_div_times.append(float(start + (end - start) * s / subdivisions))

    # Generate unique ID based on filename
    song_id = os.path.splitext(os.path.basename(file_path))[0].lower()
    song_id = song_id.replace(" ", "_").replace("-", "_")
    
    # Assemble analysis
    analysis = {
        'tempo': float(tempo),
        'beat_times': beat_times.tolist(),
        'onset_times': onset_times.tolist(),
        'rms_peak_times': rms_peak_times.tolist(),
        'ideal_beat_times': ideal_beat_times,
        'tap_targets': tap_targets,
        'spike_targets': spike_targets,
        'subdivisions': {'count': subdivisions, 'times': sub_div_times}
    }

    # Save analysis
    analysis_filename = f"{os.path.splitext(file_path)[0]}_analysis.json"
    with open(analysis_filename, 'w') as f:
        json.dump(analysis, f, indent=4)

    # Update levels.json
    levels_dir = os.path.join(os.path.dirname(os.path.dirname(file_path)), 'levels')
    levels_file = os.path.join(levels_dir, 'levels.json')
    
    # Create levels directory if it doesn't exist
    os.makedirs(levels_dir, exist_ok=True)
    
    # Load or create levels.json
    if os.path.exists(levels_file):
        with open(levels_file, 'r') as f:
            levels_data = json.load(f)
    else:
        levels_data = {"levels": []}

    # Check if song already exists in levels
    song_exists = False
    for level in levels_data["levels"]:
        if level["id"] == song_id:
            song_exists = True
            break

    if not song_exists:
        # Add new level
        new_level = {
            "id": song_id,
            "name": os.path.splitext(os.path.basename(file_path))[0],
            "musicFileName": os.path.basename(file_path),
            "analysisFileName": os.path.basename(analysis_filename),
            "difficulty": "medium"  # Default difficulty, can be adjusted
        }
        levels_data["levels"].append(new_level)
        
        # Save updated levels.json
        with open(levels_file, 'w') as f:
            json.dump(levels_data, f, indent=4)

    print(f"Analysis saved to {analysis_filename}")
    if not song_exists:
        print(f"Added new level to {levels_file}")

def process_directory(directory, hit_window, subdivisions):
    """Process all MP3 files in the directory"""
    for filename in os.listdir(directory):
        if filename.endswith('.mp3'):
            file_path = os.path.join(directory, filename)
            print(f"Processing {filename}...")
            analyze(file_path, hit_window, subdivisions)

def main():
    parser = argparse.ArgumentParser(description="Analyze audio for rhythm game.")
    parser.add_argument('--directory', default='.', help='Directory containing MP3 files')
    parser.add_argument('--hit_window', type=float, default=0.15, help='Hit window in seconds for taps')
    parser.add_argument('--subdivisions', type=int, default=1, help='Number of subdivisions per beat')
    args = parser.parse_args()
    
    process_directory(args.directory, args.hit_window, args.subdivisions)

if __name__ == '__main__':
    main()
