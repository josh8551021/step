// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimeRange> meetingTimesReq = new ArrayList<>();
    meetingTimesReq.add(TimeRange.WHOLE_DAY);
    List<TimeRange> meetingTimesOpt = new ArrayList<>();
    meetingTimesOpt.add(TimeRange.WHOLE_DAY);

    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Collections.emptyList();
    }

    // Mark unavailable times unavailable
    Collection<String> meetingAttendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    for (Event event : events) {
      Collection<String> eventAttendees = event.getAttendees();
      if (!Collections.disjoint(eventAttendees, meetingAttendees)) {
        removeTimeRangeFromFreePeriods(meetingTimesReq, event.getWhen());
        removeTimeRangeFromFreePeriods(meetingTimesOpt, event.getWhen());
      }

      if (!Collections.disjoint(eventAttendees, optionalAttendees)) {
        removeTimeRangeFromFreePeriods(meetingTimesOpt, event.getWhen());
      }
    }

    List<TimeRange> meetingTimesWithOptional = meetingTimesOpt.stream().filter(meetingTime ->
        meetingTime.duration() >= request.getDuration()).collect(Collectors.toList());
    List<TimeRange> meetingTimesWithoutOptional = meetingTimesReq.stream()
        .filter(meetingTime -> meetingTime.duration() >= request.getDuration())
        .collect(Collectors.toList());

    List<TimeRange> meetingTimesToUse = meetingTimesWithOptional;

    if (meetingTimesWithOptional.isEmpty() && !meetingAttendees.isEmpty()) {
      meetingTimesToUse = meetingTimesWithoutOptional;
    }

    Comparator<TimeRange> timeRangeComparator = Comparator.comparing(TimeRange::start);
    meetingTimesToUse.sort(timeRangeComparator);

    return meetingTimesToUse;
  }

  private void removeTimeRangeFromFreePeriods(List<TimeRange> freePeriods, TimeRange toRemove) {
    Collection<TimeRange> overlappingTimes = new HashSet<>();
    Collection<TimeRange> shortenedFreePeriods = new HashSet<>();
    freePeriods.forEach(freePeriod -> {
      if (freePeriod.overlaps(toRemove)) {
        shortenedFreePeriods.addAll(removeFreeChunk(freePeriod, toRemove));
        overlappingTimes.add(freePeriod);
      }
    });

    freePeriods.addAll(shortenedFreePeriods);
    freePeriods.removeAll(overlappingTimes);
  }

  private Collection<TimeRange> removeFreeChunk(TimeRange freePeriod, TimeRange toRemove) {
    Collection<TimeRange> newChunks = new ArrayList<>();
    if (freePeriod.start() < toRemove.start()) {
      newChunks.add(TimeRange.fromStartEnd(freePeriod.start(), toRemove.start(), false));
    }
    if (freePeriod.end() > toRemove.end()) {
      newChunks.add(TimeRange.fromStartEnd(toRemove.end(), freePeriod.end(), false));
    }
    return newChunks;
  }
}
