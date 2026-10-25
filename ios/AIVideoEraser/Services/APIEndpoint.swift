import Foundation

enum APIEndpoint {
    static var baseURL = "http://localhost:8080/api/v1"

    case register
    case login
    case profile
    case uploadVideo
    case userVideos
    case createEraseTask
    case taskDetail(String)
    case cancelTask(String)
    case plans
    case subscribe
    case feedback(String)
    case streamResult(String)

    var url: URL {
        switch self {
        case .register:
            return URL(string: "\(APIEndpoint.baseURL)/auth/register")!
        case .login:
            return URL(string: "\(APIEndpoint.baseURL)/auth/login")!
        case .profile:
            return URL(string: "\(APIEndpoint.baseURL)/auth/me")!
        case .uploadVideo:
            return URL(string: "\(APIEndpoint.baseURL)/videos/upload")!
        case .userVideos:
            return URL(string: "\(APIEndpoint.baseURL)/videos")!
        case .createEraseTask:
            return URL(string: "\(APIEndpoint.baseURL)/tasks/erase")!
        case .taskDetail(let id):
            return URL(string: "\(APIEndpoint.baseURL)/tasks/\(id)")!
        case .cancelTask(let id):
            return URL(string: "\(APIEndpoint.baseURL)/tasks/\(id)/cancel")!
        case .plans:
            return URL(string: "\(APIEndpoint.baseURL)/plans")!
        case .subscribe:
            return URL(string: "\(APIEndpoint.baseURL)/subscriptions/subscribe")!
        case .feedback(let id):
            return URL(string: "\(APIEndpoint.baseURL)/tasks/\(id)/feedback")!
        case .streamResult(let id):
            return URL(string: "\(APIEndpoint.baseURL)/files/stream/result/\(id)")!
        }
    }
}
