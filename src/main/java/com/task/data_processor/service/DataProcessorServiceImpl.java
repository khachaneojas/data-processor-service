package com.task.data_processor.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task.data_processor.dto.Location;
import com.task.data_processor.dto.MergedLocation;
import com.task.data_processor.dto.Metadata;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataProcessorServiceImpl implements DataProcessorService{

    /**
     * Loads and processes location and metadata JSON files.
     * The method reads, merges, and analyzes the data, extracting key insights.
     *
     * Processing Steps:
     * 1. Reads and parses the uploaded JSON files into corresponding Java objects.
     * 2. Merges location data with metadata to create enriched records.
     * 3. Computes statistics such as:
     *    - Count of valid locations per type.
     *    - Average ratings per location type (rounded to 2 decimal places).
     *    - Location with the highest number of reviews.
     *    - List of locations with missing metadata (incomplete data).
     * 4. Returns a structured response containing the processed data and insights.
     *
     * @param file1 The location data JSON file. Must contain a list of locations.
     * @param file2 The metadata JSON file. Must contain additional information for locations.
     * @return ResponseEntity containing processed data, statistics, and insights, or an error message if processing fails.
     */
    @Override
    public ResponseEntity<?> loadAndProcessData(
            MultipartFile file1,
            MultipartFile file2
    ) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // Read JSON files and deserialize into Java objects
            List<Location> locations = objectMapper.readValue(file1.getInputStream(),
                    new TypeReference<List<Location>>() {});
            List<Metadata> metadataList = objectMapper.readValue(file2.getInputStream(),
                    new TypeReference<List<Metadata>>() {});

            // Merge location data with metadata
            Map<String, MergedLocation> mergedData = mergeData(locations, metadataList);

            // Analyze merged data
            Map<String, Long> typeCount = mergedData.values().stream()
                    .collect(Collectors.groupingBy(MergedLocation::getType, Collectors.counting()));

            Map<String, Double> averageRatings = mergedData.values().stream()
                    .collect(Collectors.groupingBy(
                            MergedLocation::getType,
                            Collectors.collectingAndThen(
                                    Collectors.averagingDouble(MergedLocation::getRating),
                                    avg -> Math.round(avg * 100.0) / 100.0  // Rounds to 2 decimal places
                            )
                    ));

            MergedLocation mostReviewed = mergedData.values().stream()
                    .max(Comparator.comparingInt(MergedLocation::getReviews))
                    .orElse(null);

            List<String> incompleteData = locations.stream()
                    .map(Location::getId)
                    .filter(id -> !mergedData.containsKey(id))
                    .collect(Collectors.toList());

            // Prepare response with processed data and insights
            Map<String, Object> response = new HashMap<>();
            response.put("mergedData", mergedData.values());
            response.put("typeCount", typeCount);
            response.put("averageRatings", averageRatings);
            response.put("mostReviewed", mostReviewed);
            response.put("incompleteData", incompleteData);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            // Handle JSON parsing errors and invalid file formats
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid file format"));
        }
    }

    /**
     * Merges location data with metadata to create enriched location records.
     * Each location is matched with its corresponding metadata using a unique identifier.
     *
     * Merging Process:
     * 1. Converts the metadata list into a map for quick lookup based on ID.
     * 2. Iterates over the list of locations and finds the corresponding metadata entry.
     * 3. If a match is found, creates a {@link MergedLocation} object with merged details.
     * 4. Stores the merged records in a map where the key is the location ID.
     *
     * @param locations    List of {@link Location} objects containing geographic details.
     * @param metadataList List of {@link Metadata} objects containing additional information.
     * @return A map where keys are location IDs and values are {@link MergedLocation} objects containing merged data.
     */
    private Map<String, MergedLocation> mergeData(
            List<Location> locations,
            List<Metadata> metadataList
    ) {
        // Convert metadata list into a map for quick lookup
        Map<String, Metadata> metadataMap = metadataList.stream()
                .collect(Collectors.toMap(Metadata::getId, m -> m));

        // Create a map to store merged data
        Map<String, MergedLocation> mergedData = new HashMap<>();

        // Iterate through locations and merge with metadata if a match is found
        for (Location loc : locations) {
            Metadata meta = metadataMap.get(loc.getId());
            if (meta != null) {
                mergedData.put(loc.getId(), new MergedLocation(
                        loc.getId(), loc.getLatitude(), loc.getLongitude(),
                        meta.getType(), meta.getRating(), meta.getReviews()
                ));
            }
        }
        return mergedData;
    }

}
