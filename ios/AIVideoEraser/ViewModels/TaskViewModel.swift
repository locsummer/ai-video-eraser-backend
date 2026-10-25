import Foundation
import SwiftUI

@MainActor
class TaskViewModel: ObservableObject {
    @Published var currentTask: EraseTaskResponse?
    @Published var plans: [SubscriptionPlanDto] = []
    @Published var isPolling = false

    private var pollTask: Task<Void, Never>?

    func startPolling(taskId: String) {
        pollTask?.cancel()
        isPolling = true

        pollTask = Task {
            while !Task.isCancelled {
                do {
                    let task: EraseTaskResponse = try await NetworkManager.shared.request(endpoint: .taskDetail(taskId))
                    self.currentTask = task

                    if task.status == .completed || task.status == .failed || task.status == .cancelled {
                        self.isPolling = false
                        break
                    }
                } catch {
                    print("Error polling task: \(error)")
                }
                try? await Task.sleep(nanoseconds: 1_000_000_000)
            }
        }
    }

    func cancelTask(taskId: String) async {
        do {
            let task: EraseTaskResponse = try await NetworkManager.shared.request(
                endpoint: .cancelTask(taskId),
                method: "POST"
            )
            self.currentTask = task
            self.isPolling = false
        } catch {
            print("Failed to cancel: \(error)")
        }
    }

    func submitFeedback(taskId: String, rating: Int, comment: String?) async {
        do {
            let req = FeedbackRequest(rating: rating, comment: comment, category: "INPAINTING_QUALITY")
            let body = try JSONEncoder().encode(req)
            let _: FeedbackResponse = try await NetworkManager.shared.request(
                endpoint: .feedback(taskId),
                method: "POST",
                body: body
            )
        } catch {
            print("Failed feedback: \(error)")
        }
    }

    func loadPlans() async {
        do {
            let plans: [SubscriptionPlanDto] = try await NetworkManager.shared.request(endpoint: .plans)
            self.plans = plans
        } catch {
            print("Failed to load plans: \(error)")
        }
    }

    func subscribe(planId: String) async {
        do {
            let req = SubscribeRequest(planId: planId, paymentToken: nil)
            let body = try JSONEncoder().encode(req)
            let _: SubscriptionResponse = try await NetworkManager.shared.request(
                endpoint: .subscribe,
                method: "POST",
                body: body
            )
        } catch {
            print("Failed subscribe: \(error)")
        }
    }
}
