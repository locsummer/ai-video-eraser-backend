import Foundation

class NetworkManager {
    static let shared = NetworkManager()
    private init() {}

    var authToken: String? {
        get { UserDefaults.standard.string(forKey: "auth_token") }
        set { UserDefaults.standard.set(newValue, forKey: "auth_token") }
    }

    func request<T: Codable>(
        endpoint: APIEndpoint,
        method: String = "GET",
        body: Data? = nil
    ) async throws -> T {
        var req = URLRequest(url: endpoint.url)
        req.httpMethod = method
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")

        if let token = authToken {
            req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        if let body = body {
            req.httpBody = body
        }

        let (data, response) = try await URLSession.shared.data(for: req)

        guard let httpResponse = response as? HTTPURLResponse, (200...299).contains(httpResponse.statusCode) else {
            let errorMsg = String(data: data, encoding: .utf8) ?? "Unknown server error"
            throw NSError(domain: "NetworkManager", code: -1, userInfo: [NSLocalizedDescriptionKey: errorMsg])
        }

        let decoder = JSONDecoder()
        let apiResponse = try decoder.decode(ApiResponse<T>.self, from: data)
        guard let responseData = apiResponse.data else {
            throw NSError(domain: "NetworkManager", code: -2, userInfo: [NSLocalizedDescriptionKey: apiResponse.message ?? "No data returned"])
        }

        return responseData
    }

    func uploadVideo(fileURL: URL) async throws -> VideoResponse {
        var req = URLRequest(url: APIEndpoint.uploadVideo.url)
        req.httpMethod = "POST"

        let boundary = "Boundary-\(UUID().uuidString)"
        req.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")

        if let token = authToken {
            req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        let videoData = try Data(contentsOf: fileURL)
        var body = Data()

        body.append("--\(boundary)\r\n".data(using: .utf8)!)
        body.append("Content-Disposition: form-data; name=\"file\"; filename=\"\(fileURL.lastPathComponent)\"\r\n".data(using: .utf8)!)
        body.append("Content-Type: video/mp4\r\n\r\n".data(using: .utf8)!)
        body.append(videoData)
        body.append("\r\n".data(using: .utf8)!)
        body.append("--\(boundary)--\r\n".data(using: .utf8)!)

        req.httpBody = body

        let (data, response) = try await URLSession.shared.data(for: req)
        guard let httpResponse = response as? HTTPURLResponse, (200...299).contains(httpResponse.statusCode) else {
            throw NSError(domain: "NetworkManager", code: -1, userInfo: [NSLocalizedDescriptionKey: "Video upload failed"])
        }

        let apiResponse = try JSONDecoder().decode(ApiResponse<VideoResponse>.self, from: data)
        guard let video = apiResponse.data else {
            throw NSError(domain: "NetworkManager", code: -2, userInfo: [NSLocalizedDescriptionKey: "No video response"])
        }

        return video
    }
}
