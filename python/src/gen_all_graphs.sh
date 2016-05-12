#!/bin/bash
python drop_one_align_feature.py &
python drop_one_ws_feature.py &
python leave_one_out.py &
python subset_validator.py &
python vary_k.py &
python antlr_one_file_capture.py &
python java8_one_file_capture.py &
python java_one_file_capture.py &
python sqlite_noisy_one_file_capture.py &
python sqlite_one_file_capture.py &
python tsql_noisy_one_file_capture.py &
python tsql_one_file_capture.py &
python quorum_one_file_capture.py &
