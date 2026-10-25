import SwiftUI

struct SubscriptionView: View {
    @StateObject private var taskViewModel = TaskViewModel()
    @Environment(\.dismiss) private var dismiss
    @State private var selectedPlanId = "PRO_MONTHLY"

    var body: some View {
        ZStack {
            AppTheme.darkBackground.ignoresSafeArea()

            ScrollView {
                VStack(spacing: 20) {
                    HStack {
                        Spacer()
                        Button {
                            dismiss()
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .font(.system(size: 26))
                                .foregroundColor(AppTheme.textSecondary)
                        }
                    }

                    // Diamond Icon
                    ZStack {
                        Circle()
                            .fill(Color.yellow.opacity(0.15))
                            .frame(width: 64, height: 64)
                        Image(systemName: "crown.fill")
                            .font(.system(size: 32))
                            .foregroundColor(.yellow)
                    }

                    Text("Unlock AI Video Eraser Pro")
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(.white)

                    Text("Highest quality inpainting without restrictions")
                        .font(.system(size: 13))
                        .foregroundColor(AppTheme.textSecondary)

                    // Benefits
                    VStack(alignment: .leading, spacing: 12) {
                        BenefitItem(text: "Remove unlimited objects, people & logos")
                        BenefitItem(text: "Export in Full HD 1080p & 4K Ultra")
                        BenefitItem(text: "Up to 300 seconds video length")
                        BenefitItem(text: "Priority Dedicated GPU Cloud Queue")
                        BenefitItem(text: "100% Watermark-free renders")
                    }
                    .padding()
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(AppTheme.surfaceDark)
                    .cornerRadius(16)

                    // Plan selection
                    VStack(spacing: 12) {
                        PlanCard(
                            id: "PRO_MONTHLY",
                            title: "Pro Monthly",
                            price: "$9.99 / month",
                            credits: "300 Credits",
                            isSelected: selectedPlanId == "PRO_MONTHLY"
                        ) {
                            selectedPlanId = "PRO_MONTHLY"
                        }

                        PlanCard(
                            id: "PRO_YEARLY",
                            title: "Pro Yearly (Save 50%)",
                            price: "$59.99 / year",
                            credits: "500 Credits",
                            isSelected: selectedPlanId == "PRO_YEARLY"
                        ) {
                            selectedPlanId = "PRO_YEARLY"
                        }

                        PlanCard(
                            id: "VIP_LIFETIME",
                            title: "VIP Lifetime",
                            price: "$99.99 (One-time)",
                            credits: "Unlimited Credits",
                            isSelected: selectedPlanId == "VIP_LIFETIME"
                        ) {
                            selectedPlanId = "VIP_LIFETIME"
                        }
                    }

                    // Continue Button
                    Button {
                        Task {
                            await taskViewModel.subscribe(planId: selectedPlanId)
                            dismiss()
                        }
                    } label: {
                        Text("Continue")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 52)
                            .background(AppTheme.accentPurple)
                            .cornerRadius(14)
                    }
                    .padding(.top, 10)
                }
                .padding(24)
            }
        }
        .task {
            await taskViewModel.loadPlans()
        }
    }
}

struct BenefitItem: View {
    let text: String

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: "checkmark.circle.fill")
                .foregroundColor(AppTheme.successGreen)
            Text(text)
                .font(.system(size: 13))
                .foregroundColor(.white)
        }
    }
}

struct PlanCard: View {
    let id: String
    let title: String
    let price: String
    let credits: String
    let isSelected: Bool
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(.system(size: 15, weight: .bold))
                        .foregroundColor(.white)
                    Text(credits)
                        .font(.system(size: 12))
                        .foregroundColor(AppTheme.textSecondary)
                }
                Spacer()
                Text(price)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(isSelected ? AppTheme.accentPurple : .white)
            }
            .padding(16)
            .background(isSelected ? AppTheme.surfaceDark : AppTheme.darkBackground)
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(isSelected ? AppTheme.accentPurple : AppTheme.surfaceLight, lineWidth: isSelected ? 2 : 1)
            )
            .cornerRadius(14)
        }
    }
}
