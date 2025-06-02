/**
 * Interactive Star Rating Component
 * Provides star-based rating functionality with mouse hover and click interactions
 */
class StarRating {
    constructor(element) {
        this.container = element;

        this.readonly = element.dataset.canRate ? element.dataset.canRate === "false" : true;
        this.averageRating = element.dataset.averageRating ? parseFloat(element.dataset.averageRating) : null;
        this.userRating = element.dataset.userRating ? parseFloat(element.dataset.userRating) : null;
        if (this.userRating === -1) {
            this.userRating = null;
        }
        this.ratingCount = element.dataset.ratingCount ? parseInt(element.dataset.ratingCount) : 0;
        this.mapId = element.dataset.mapId;
        this.teamId = element.dataset.teamId;

        this.hoveredRating = 0;

        this.container.innerHTML = '';

        // data-bs-toggle="tooltip" data-bs-placement="bottom" data-bs-title="Tooltip on bottom"
        this.average = document.createElement('span');
        this.average.dataset.bsPlacement = "bottom"
        this.average.dataset.bsOriginalTitle = "Average"
        for (let i = 1; i <= 5; i++) {
            const star = document.createElement('span');
            star.classList.add('star');
            star.classList.add('average');
            star.dataset.rating = i;
            star.innerHTML = '★';
            this.average.appendChild(star);
        }
        this.container.appendChild(this.average);
        new bootstrap.Tooltip(this.average, {
            container: 'body'
        });

        if (!this.readonly) {
            this.user = document.createElement('span');
            this.user.dataset.bsPlacement = "bottom"
            this.user.dataset.bsOriginalTitle = "Average"

            const start = document.createElement('span');
            start.textContent = '(';
            this.user.appendChild(start);

            for (let i = 1; i <= 5; i++) {
                const star = document.createElement('span');
                star.classList.add('star');
                star.classList.add('user');
                star.dataset.rating = i;
                star.innerHTML = '★';
                this.user.appendChild(star);
            }

            this.deleteUserRating = document.createElement('span');
            this.deleteUserRating.classList.add('star');
            this.deleteUserRating.classList.add('user');
            this.deleteUserRating.classList.add('cancel');
            this.deleteUserRating.classList.add('disabled');
            this.deleteUserRating.dataset.rating = -1;
            this.deleteUserRating.innerHTML = '🗙';
            this.user.appendChild(this.deleteUserRating);

            const end = document.createElement('span');
            end.textContent = ')';
            this.user.appendChild(end);

            this.container.appendChild(this.user);
            new bootstrap.Tooltip(this.user, {
                container: 'body'
            });
        }

        if (!this.readonly) {
            this.bindEvents();
        }

        this.updateStarDisplay(1);
    }

    bindEvents() {
        const stars = this.container.querySelectorAll('.star.user');

        stars.forEach(star => {
            star.addEventListener('mouseenter', (e) => {
                this.hoveredRating = parseInt(e.target.dataset.rating);
                this.updateStarDisplay(e.target.dataset.rating);
            });

            star.addEventListener('click', (e) => {
                const rating = parseInt(e.target.dataset.rating);
                this.setRating(rating);
            });

            star.addEventListener('mouseleave', () => {
                this.hoveredRating = 0;
                this.updateStarDisplay(0);
            });
        });
    }

    updateStarDisplay(userStar) {
        const averageStars = this.container.querySelectorAll('.star.average');
        averageStars.forEach((star, index) => {
            const starRating = index + 1;
            star.classList.remove('filled', 'empty', 'partial');
            star.style.removeProperty('--fill-percentage');
            if (this.averageRating) {
                if (starRating <= Math.floor(this.averageRating)) {
                    star.classList.add('filled');
                } else if (starRating === Math.ceil(this.averageRating) && this.averageRating % 1 !== 0) {
                    star.classList.add('partial');
                    const percentage = 20 + ((this.averageRating % 1) * 60).toFixed(1);
                    star.style.setProperty('--fill-percentage', `${percentage}%`);
                } else {
                    // Étoile vide
                    star.classList.add('empty');
                }
            } else {
                star.classList.add('empty');
            }
        });

        if (!this.readonly) {
            var displayRating;
            if (this.hoveredRating > 0) {
                displayRating = this.hoveredRating
            } else if (this.userRating) {
                displayRating = this.userRating;
            } else {
                displayRating = 0;
            }

            const userStars = this.container.querySelectorAll('.star.user');
            userStars.forEach((star, index) => {
                const starRating = index + 1;
                star.classList.remove('filled', 'empty', 'hovered');
                if (starRating <= this.hoveredRating) {
                    star.classList.add('hovered');
                } else if (starRating <= displayRating) {
                    star.classList.add('filled');
                } else {
                    star.classList.add('empty');
                }
            });

            if (this.userRating) {
                this.deleteUserRating.classList.remove('disabled');
                this.deleteUserRating.classList.add('enabled');
                if (userStar === -1) {
                    this.user.dataset.bsOriginalTitle = `Supprimer votre note`;
                } else {
                    this.user.dataset.bsOriginalTitle = `Votre note : ${this.userRating}`;
                }
            } else {
                this.deleteUserRating.classList.remove('enabled');
                this.deleteUserRating.classList.add('disabled');
                this.user.dataset.bsOriginalTitle = `Vous n'avez pas encore voté`;
            }
        }

        if (this.averageRating && this.ratingCount && this.ratingCount > 0) {
            var avg = this.averageRating;
            if (avg === Math.round(avg)) {
                avg = this.averageRating.toFixed(0);
            } else {
                avg = this.averageRating.toFixed(1);
            }
            this.average.dataset.bsOriginalTitle = `${avg}/5 (${this.ratingCount} ${this.ratingCount === 1 ? 'vote' : 'votes'})`;
        }
    }

    setRating(rating) {
        if (this.readonly) return;

        // Show loading state
        this.container.classList.add('loading');

        const url = `/${this.teamId}/maps/${this.mapId}/rate`;

        fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `rating=${rating}`
        })
            .then(response => {
            if (!response.ok) {
                throw new Error('Failed to submit rating');
            }
            return response.json();
        })
            .then(data => {
            // Update the rating display with new data
            this.averageRating = data.averageRating;
            this.ratingCount = data.ratingCount;
            if (data.userRating) {
                this.userRating = data.userRating;
            } else {
                this.userRating = null;
            }
        })
            .catch(error => {
            console.error('Error submitting rating:', error);
        })
            .finally(() => {
            this.updateStarDisplay(1);
            this.container.classList.remove('loading');
        });
    }

}

// Auto-initialize all star ratings on page load
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.star-rating').forEach(element => new StarRating(element));
});
