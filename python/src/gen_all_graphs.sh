#!/bin/bash
python drop_one_align_feature.py &
python drop_one_ws_feature.py &
python leave_one_out.py &
python subset_validator.py &
python vary_k.py &
